# ECGMonitorWithNRF  

<img src="https://img.shields.io/badge/Android-v1.0.0-3DDC84?logo=android">  

[Nordic - nrf52840](https://www.nordicsemi.com/Products/Low-power-short-range-wireless/nRF52840) 블루투스 동글을 이용한 ECG ploat 및 심전도 관련 헬스케어 어플리케이션  

사용자의 심전도를 24시간 측정하는 IoT 기기(nrf52840)로부터 실시간으로 데이터를 전송받아 저장/가공/분석해주는 헬스케어 어플리케이션을 계획하고 제작하였습니다. nrf52840과 최초 1회 연결 후에는 의도적으로 연결을 끊지 않는 한 연결이 지속돼야하며, 연결이 불안정하거나 기기와의 거리가 멀어져 연결이 잠시 끊어지더라도 연결 가능한 상태가 되면 자동으로 연결되도록 구현하는 것이 중요했습니다. 또한 1초당 수십개의 데이터를 수신하기 때문에 이를 처리하는 과정에서 발생하는 많은 예외 상황들에 대처하고 UI 갱신이나 앱 퍼포먼스에 끼치는 영향을 최소화하기 위해 스레드와 객체 풀을 활용한 난이도 있는 프로젝트입니다.

## 기능  

### 스캔 및 연결  

<img width="200" src="https://user-images.githubusercontent.com/57310034/110596615-d7daa080-81c2-11eb-800b-d0561d806bff.png">  

BLE 스캔을 사용하여 주변에 있는 블루투스 기기들을 찾습니다. rssi, 연결 가능 여부 등으로 필터링한 뒤, 약속된 uuid와 비교하여 nrf52840을 특정합니다.  

특정된 nrf52840과는 `BluetoothDevice#connectGatt()`를 사용하여 연결을 시도합니다. 이때 스마트폰 단말기는 서버의 역할을 하며, nrf52840은 클라이언트의 역할을 하게 됩니다. 그리고 `autuConnect` 파라미터를 `true`로 설정하여 의도적인 연결 해제 이 외에는 자동으로 연결되도록 설정하였습니다.  

연결 시, nrf52840이 심전도 데이터를 작성하는 characteristic을 찾아 Notification 설정을 켜고 값이 변경되면 앱 전역 `HeartBeatSampleLiveData`의 값을 변경하도록 하였습니다.

### 심전도 그래프 출력  

<div><img width="200" hspace="20" src="https://user-images.githubusercontent.com/57310034/110598115-bc709500-81c4-11eb-9bb9-a8fe8898e0f6.png"/> 
<img height="200" src="https://user-images.githubusercontent.com/57310034/110606741-d662a580-81cd-11eb-95af-0162ece54bad.png"/></div>

nrf52840이 작성한 심전도 데이터는 앱이 실시간으로 읽어들여 앱 전역에 선언된 `HeartBeatSampleLiveData`의 값을 변경시킵니다. 해당 라이브데이터를 구독하는 컴포넌트 중 하나는 심전도 그래프를 그리는 프래그먼트입니다. 프래그먼트가 커스텀 뷰에 심전도 데이터와 데이터 변경을 감지한 시간을 입력하면 커스텀 뷰가 실시간으로 그래프를 그려주는 방식입니다. 그래프는 커스텀뷰의 왼쪽부터 그려지며 오른쪽 끝에 닿으면 왼쪽 끝부터 최소한의 데이터만 덮어쓰며 다시 시작합니다. 이 주기는 현재 10초에 한 번씩 반복되고 있으며 화면 크기의 변화에도 주기에는 영향이 없도록, 결정된 뷰의 가로길이에 따라 유동적으로 그래프의 scale이 결정됩니다. 심전도 데이터가 감지되지 않더라도 그래프는 가로 중심선을 따라 균일하게 그려집니다.  

심전도 데이터값과 각 데이터의 변경 시간을 가진 객체가 1초에 수십개씩 발생하기 때문에 객체 풀 패턴을 활용하여 객체를 재활용하였고, 앱의 퍼포먼스를 증가시켰습니다.  

### 심박수 계산  

심박수는 앱의 포그라운드에 있을 때나 그렇지 않을 때에도 계산되어야 하므로 `ForegroundService`에서 이뤄집니다. 해당 서비스는 nrf52840을 스캔하여 특정한 시점부터 실행되어, 의도적으로 연결을 해제할 때 까지 종료되지 않습니다. 또한 이 서비스는 `HeartRateSampleLiveData`를 구독하고 있는 컴포넌트로써 nrf52840이 측정한 심전도 데이터를 실시간으로 받아들여 다양한 모듈에 입력하는 역할을 하고 있습니다. 심박수를 계산하는 모듈인 `BPMCalculater`도 여기에 속합니다.  

<img src="https://user-images.githubusercontent.com/57310034/111024118-3b153e80-8420-11eb-8e93-f87c5ec9494e.png"/>  

`BPMCalculater`는 `ScheduledThreadPoolExecutor`를 사용합니다. `ForegroundService`로 부터 입력받은 심전도 데이터들을 큐에 담아두고 주기적으로 큐를 비워가며 심박수를 계산하기 위해서입니다. 심전도 그래프 프래그먼트에서 보여줄 예상 심박수를 3초에 한 번씩 계산하며, 60초에 한 번 데이터베이스에 저장하기 위한 정확한 심박수를 계산합니다. 심박수는 60초당 발생한 R-peak의 개수를 세어 계산되었고 이때 사용한 알고리즘은 다음 논문을 참고하였습니다.  
[적응형 필터와 가변 임계값을 적용하여 잡음에 강인한 심전도 R-피크 검출](http://jkais99.org/journal/Vol18No12/vol18no12p18.pdf)  

### 빈맥/서맥 검출 및 Notification 발생과 SMS 전송

<img width="300" src="https://user-images.githubusercontent.com/57310034/110605495-89ca9a80-81cc-11eb-88d0-923e338b01a1.png"/>  

*(괄호 안은 평균 심박수로, 이미지는 더미 데이터로 제작되었음)*

계산된 심박수를 토대로 빈맥과 서맥을 검출합니다. 데이터베이스에 저장된 심박수와 비교하여 최근 10분간 심박수가 정삼 범위에서 벗어나면 빈맥/서맥으로 판단합니다. 이때 해당 사실을 알리기 위해 평균 심박수를 포함한 경고 메시지를 담은 Notification을 발생시킵니다. 동시에, 미리 등록해둔 연락처로 이와 같은 맥락의 SMS 메시지를 전송합니다.  

### 심박수 추이 그래프  

<div><img width="200" hspace="20" src="https://user-images.githubusercontent.com/57310034/110605756-c7c7be80-81cc-11eb-8aeb-700859b3051f.png"/>
<img width="200" src="https://user-images.githubusercontent.com/57310034/110606618-b337f600-81cd-11eb-9db4-b7487b8deda6.png"/></div>

데이터베이스에 저장된 심박수 그래프를 토대로 심박수 추이 그래프를 그립니다. 해당 프래그먼트의 상단에는 최초 기록이 존재하는 날부터 오늘 날까지의 일자를 선택할 수 있는 가로 리사이클러뷰가 존재합니다. 이는 리사이클러뷰를 상속한 커스텀뷰로 작성되었습니다. 여기서 선택한 날에 기록된 심박수 추이 그래프가 그 아래에 위치한 커스텀뷰에 그려지게됩니다. 이 그래프는 가로로 스크롤이 가능하며 왼쪽 끝은 00시, 오른쪽 끝은 23시 59분을 나타냅니다. 심박수의 최대값과 최소값에 따라 그래프가 한 눈에 모두 보이도록 세로 scale이 계산되며, 연결 불량 및 해제 등으로 심전도 데이터가 연속적이지 않은 경우, 둥근 점으로 그래프가 마무리되거나 다시 시작됩니다.  

### 설정  

<img width="200" src="https://user-images.githubusercontent.com/57310034/110606913-0611ad80-81ce-11eb-9e0d-f97bc13cc2cf.png"/>  

`PreferenceFragment`로 작성되었으며, 푸시 알림 및 문자 알림 기능을 on/off할 수 있습니다. 또한 SMS 문자 전송에 사용될 연락처를 직접 입력하거나 스마트폰 주소록에서 가져와 데이터베이스에 등록할 수 있고, SMS 전송 문구에 포함될 사용자의 이름을 저장할 수 있습니다.  

<br>

## 구조  

<img src="https://user-images.githubusercontent.com/57310034/111024053-ee316800-841f-11eb-8e1a-e8f51a705996.png"/>  

심박수 계산 및 분석, 저장 등 앱의 핵심 기능은 실행 중인 액티비티가 없더라도 계속 동작하여야 합니다. 따라서 위 핵심 기능들은 `ForegroundService` 에서 실행됩니다. `ForegroundService`는 nrf52840가 측정한 심전도 데이터를 지속적으로 갱신하고 있는 `HeartBeatSampleLiveData`를 구독합니다. 서비스는 여기서 얻은 심전도 데이터를 `BPMManager` 모듈로 전달합니다. `BPMManager`는 Pacade 패턴으로 구현되어있으며 `BPMCalculator`(BPM 계산), `AbnormalBPMDetector`(이상 심박수 패턴 검출), `AbnormalProtocol`(푸시 알림 및 SMS 전송) 등의 모듈을 포함하여 제어합니다.  

만약 nrf52840과 연결이 잠시 끊어지거나 스마트폰의 블루투스 기능을 잠깐 끄는 등 연결 상태에 변화가 생기면 서비스나 액티비티 모두에게 영향을 끼칩니다. 서비스는 심전도 데이터를 연산하는 작업을 멈추어야할 것이고 액티비티는 그래프 출력을 중단하고 사용자에게 현재 상황을 알려주어야합니다.  

nrf52840의 연결 상태는 `BluetoothConnectStateLiveData` 객체를 갱신하고 있습니다. 그리고 해당 객체는 갱신된 상태에 따라 `BluetoothConnectStateCallback`의 메서드들을 호출하고 있습니다. 서비스와 액티비티는 이 `BluetoothConnectStateCallback`를 구현함과 동시에 `BluetoothConnectStateLiveData`를 구독하고 있습니다. 따라서 서비스와 액티비티는 nrf52840의 연결 상태에 따라 각자의 대응 코드를 실행합니다.

<br>

## 사용 라이브러리  

- [scanner](https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library) : 안드로이드 표준 `BluetoothLeScanner`를 확장하여 호환성에 대처하기 위해 사용하였습니다.  
- [hilt-android](https://github.com/google/dagger) : DI를 간편하게 적용하기 위해 사용하였습니다.  
- [materialbanner](https://github.com/sergivonavi/MaterialBanner) : Material 테마의 banner 컴포넌트를 추가하기 위해 사용하였습니다.
- 그 외 : `Room`, `navigation-fragment-ktx`, `lifecycle-extensions` 등  

<br>

## 개발 환경  

- IDE : AndroidStudio 4.1.1
- Platform : Android
- SdkVersion
    - compile : 30
    - target : 30
    - min : 23
- Language : Kotlin

<br>

## 개발자  

- [김성규](https://github.com/SEONGGYU96) (단독)