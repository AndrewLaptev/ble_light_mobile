<div id="top"></div>


<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/AndrewLaptev/ble_light_mobile">
    <img src="docs/images/logo.png" alt="Logo" width="125" height="125">
  </a>

<h2 align="center">BLight</h2>

  <p align="center">
    Мобильное приложение для мультиканального управления умными лампами
    <br />
      <a href="https://github.com/AndrewLaptev/ble_light_esp32"><strong>ble_light_esp32</strong></a>
    <br />
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->
## About

Данное мобильное приложение предназначено для ручного беспроводного управления сразу несколькими умными лампами в определенной области действия. В качестве управляющего контроллера лампы используется микроконтроллер ESP32 с прошивкой [BLightESP32](https://github.com/AndrewLaptev/ble_light_esp32). Управление осуществляется через изменение цветовой температуры ламп, а также их яркости.

### Built With
* [![Android Studio][android-studio-shield]][android-studio-url]
* [![Pikolo][pikolo-shield]][pikolo-url]

### Requirements
* Android 10 и выше
* Bluetooth 5.0

<!-- USAGE -->
## Usage

<details>
<summary><h4>Step-by-step instruction</h4></summary>

  ### Setup

  Перед подключением приложения к лампам с прошивкой BLightESP32 можно произвести настройку в разделе `Settings` (выпадающее меню на главном экране):
  <p align="center">
    <a href="https://github.com/AndrewLaptev/ble_light_mobile">
        <img src="docs/images/img10.png" alt="App screenshot 10" width="230" height="480">
    </a>
  </p>

  ### User mode

  Для использования приложения вам нужно будет дать разрешение на доступ приложения к Bluetooth и вашему местоположению на устройстве

  1) Запускаем приложение:
  <p align="center">
    <a href="https://github.com/AndrewLaptev/ble_light_mobile">
        <img src="docs/images/img1.png" alt="App screenshot 1" width="230" height="480">
    </a>
  </p>

  2) Выставляем ползунок `RSSI threshold` на необходимое значение, которое измеряется в `dBm` и интерпретирует расстояние до лампы. Чем больше по модулю будет показание `RSSI threshold`, тем больше будет область поиска ламп. После чего нажимаем кнопку поиска:
  <p align="center">
    <a href="https://github.com/AndrewLaptev/ble_light_mobile">
        <img src="docs/images/img2.png" alt="App screenshot 2" width="230" height="480">
    </a>
  </p>

  3) Если лампы будут найдены, то на экране отобразится панель управления:
  <p align="center">
    <a href="https://github.com/AndrewLaptev/ble_light_mobile">
        <img src="docs/images/img3.png" alt="App screenshot 3" width="230" height="480">
    </a>
  </p>

  4) С помощью ползунков выбираем нужные значения цветовой температуры и яркости, после чего нажимаем кнопку `Send`, которая отправляет выбранный режим на все подключенные лампы:
  <p align="center">
    <a href="https://github.com/AndrewLaptev/ble_light_mobile">
        <img src="docs/images/img4.png" alt="App screenshot 4" width="230" height="480">
    </a>
  </p>

  5) Чтобы отключиться от ламп, достаточно просто перейти на главный экран приложения через кнопку действия мобильного телефона `Назад`

  ### Developer mode

  В приложении также имеется продвинутый режим использования, который позволяет видеть все ближайшие устройства Bluetooth, а также их RSSI в реальном времени, сервисы и значения характеристик

  1) Чтобы перейти в продвинутый режим, нужно на главном экране приложения справа сверху нажать значок выпадающего меню и выбрать пункт `Developer mode`:
  <p align="center">
    <a href="https://github.com/AndrewLaptev/ble_light_mobile">
        <img src="docs/images/img5.png" alt="App screenshot 5" width="230" height="480">
    </a>
  </p>

  2) После нажатия кнопки `Scan` на экране появится список из всех доступных Bluetooth устройств поблизости с отображением их имен, MAC-адресов и уровней RSSI:
  <p align="center">
    <a href="https://github.com/AndrewLaptev/ble_light_mobile">
        <img src="docs/images/img6.png" alt="App screenshot 6" width="230" height="480">
    </a>
  </p>

  3) При нажатии на устройство из списка отобразится диалоговое окное, которое содержит имя устройства, его адрес, тип Bluetooth устройства, а также конопки подключения и выхода из диалогового окна:
  <p align="center">
    <a href="https://github.com/AndrewLaptev/ble_light_mobile">
        <img src="docs/images/img7.png" alt="App screenshot 7" width="230" height="480">
    </a>
  </p>

  4) После удачного подключения к устройству на экране отобразятся все его сервисы и характеристики. Узнать значения характеристики можно нажав на нее, также отобразится тип характеристики во всплывающем сообщении (`Readable`, `Writeble` и т.д.). Если характеристика `Writeble`, то появится диалоговое окно для записи значения:
  <p align="center">
    <a href="https://github.com/AndrewLaptev/ble_light_mobile">
        <img src="docs/images/img8.png" alt="App screenshot 8" width="230" height="480">
    </a>
  </p>

  5) В интерфейсе поиска устройств рядом с кнопкой `Scan` также есть еще кнопки `Filter on/off` и `Multiple connect`. Первая из них включает фильтр при поиске устройств и отображает только те устройства, на которых установлена заранее настроенная прошивка BLightESP32, вторая кнопка запускает множественное подключение ко всем найденным устройствам с прошивкой BLightESP32:
  <p align="center">
    <a href="https://github.com/AndrewLaptev/ble_light_mobile">
        <img src="docs/images/img9.png" alt="App screenshot 9" width="230" height="480">
    </a>
  </p>

  <p align="right">(<a href="#top">back to top</a>)</p>
</details>

<!-- GETTING STARTED -->
## Getting Started

Здесь находится описание того, как можно установить и запустить приложение из исходных файлов

### Prerequisites

* Android Studio (2021.1.1 и выше)

### Installation

1. Клонируем репозиторий
   ```bash
   git clone https://github.com/AndrewLaptev/ble_light_mobile
   ```
2. Открываем проект в Android Studio
3. Выполняем сборку проекта и закачиваем его на телефон (в эмуляторе приложение работать не будет)

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- DOCUMENTATION -->
## Documentation

В данном разделе описаны функциональные модули приложени и классы (Activity), из которых они состоят. 

Рассматриваются только методы, в которых содержится специфическая для данного приложения логика, т.е. стандартные методы `onCreate()`, `onResume()` и т.п., **не несущие** в себе специфической логики, описаны не будут. 

Не рассматриваются подробно классы, методы и функции (или часть метода) отрисовки интерфейса приложения, т.к. это простой базовый интерфейс, сделанный для прототипа.

<details>
  <summary><h3>ble_light</h3></summary>
  Данный функциональный модуль является основным в работе приложения и реализует пользовательский (User mode) режим работы приложения.
  
  #### MainActivity
  Главный класс приложения, в нем находится entrypoint, доступ к настройкам, продвинутому режиму и именно от него запускается сканирование устройств с последующим подключением
  
  *Protected* методы:
  * `onCreate` - вызывает приватные методы для инициализации, настройки и сканировании Bluetooh, а также права доступа для Bluetooth и проверку на поддержку BLE.
  
  *Private* методы:
  * `getBluetoothAdapterAndLeScanner` - инициализирует объекты классов `BluetoothAdapter` и `BluetoothLeScanner`
  * `scanBleDevices` - запускает сканирование устройств Bluetooth с определенным `scan_period` временем, передает список адресов устройств в класс `LightManageActivity`
  * `meanRSSI` - высчитывает среднее значение силы сигнала RSSI по вхожному массиву значений
  * `loadSettings` - подгружает настройки (`scan_period`) из `root_preferences.xml`
  
  *Inner* классы:
  * `Kalman` - класс, реализующий фильтр Калмана, имеет один *public* метод `filter(int init_rssi, ArrayList<Integer> rssi_list)`, который непосредственно производит фильтрацию
  
  #### BluetoothLeService
  Класс, содержащий в себе все необходимые методы для создания и управления Bluetooth соеднинений.
  
  *Public* методы:
  * `initialize` - инициализирует объект класса `BluetoothAdapter`
  * `connect` - выполнеяет подключение к GATT устройства Bluetooth по указанному MAC адресу
  * `multiconnect` - выполняет подключение сразу в нескольким GATT устройств Bluetooth по указанному списку MAC адресов
  * `disconnect` - производит отключение от одного или сразу нескольких Bluetooth устройств
  * `сlose` - закрывает соединение/соединения GATT устройств Bluetooth
  * `readCharacteristic` - производит чтение значения определенной характеристики GATT одного Bluetooth устройства или сразу нескольких
  * `writeCharacteristic` - производит запись значения в определенную характеристику GATT одного Bluetooth устройства или сразу нескольких
  * `setCharacteristicNotification` - устанавливает или отключает уведомление на определенную характеристику GATT одного Bluetooth устройства или сразу нескольких
  * `getSupportedGattServices` - возвращает список доступных сервисов GATT Bluetooth устройства
  
  *Private* методы:
  * `loadSettings` - подгружает настройки (`reconnection_attempts`) из `root_preferences.xml`
  
  *Inner* классы:
  * `BluetoothGattExt` - класс, являющийся расширением класса `BluetoothGatt`. Более тесно связывает Bluetooth устройство и объект `BluetoothGatt`, т.к. стандартный `BluetoothGatt` может подключаться к нескольким устройствам, что не обеспечивает обмена данными сразу с несколькими устройствами

  #### LightManageActivity
  Класс, отвечающий за аутентификацию и управление световыми режимами на подключенном Bluetooth устройстве (сервисы `Authentication` и `Light manage`). Аутентификация нужна только для доступа к записи значений в характеристику `Level of light` для управления световыми режимами, т.е. можно подключиться к GATT устройства BLightESP32 и увидеть его сервисы и характеристики, но, без аутентификации (`access_token`) через характеристику `Authorization data`, будет невозможно управлять световым режимом.
  
  *Protected* методы:
  * `onCreate` - производит начальные вычисления шага изменения светового режима на основе подруженных настроек, инициаилизирует визуальный интерфейс выбора режима, производит инициализацию подключения к Bluetooth устройствам BLightESP32
  
  *Private* методы:
  * `initServiceConnection` - производит подключение к Bluetooth устройствам, также определяет метод обратного вызова для переподключения, если подключиться с первого раза не получается
  * `authDataSending` - передает аутентификационные данные (`access_token`) в характеристику `Authorization data` для будущего доступа к записи значений светового режима в характеристику `Level of light`
  * `loadSettings` - подгружает настройки (`access_token`, `effect_color_temp_min_key`, `effect_color_temp_max_key`) из `root_preferences.xml`
 
  #### SettingsActivity
  Класс, реализующий интерфейс взаимодействия пользователя с настройками приложения через `root_preferences.xml`, позволяет сброить настройки до значений по умолчанию. В основном этот класс содержит визуальный функционал, поэтому подробно описан не будет.

</details>


<details>
  <summary><h3>dev_mode</h3></summary>
  Данный модуль реализует продвинутый режим (Developer mode) использования приложения.
  
  #### MainActivityDev
  Главный класс для взаимодействия с устройствами Bluetooth, реализует графический интерфейс представления всех доступных устройств в радиусе действия Bluetooth.
  
  *Protected* методы:
  * `onCreate` - вызывает приватные методы для инициализации, настройки и сканировании Bluetooh, определяет функции обратного вызова для кнопок `Filter ON/OFF` и `Multiple connect`
  
  *Private* методы:
  * `getBTDeviceType` - определяет тип выбранного Bluetooth устройства перед непосредственным подключением
  * `getBluetoothAdapterAndLeScanner` - инициализирует объекты классов `BluetoothAdapter` и `BluetoothLeScanner`
  * `scanLeDevice` - по кнопке `SCAN`запускает сканирование устройств, до тех пор, пока не будет нажата кнопка `STOP`
  
  *Inner* классы:
  * `BluetoothDeviceExt` - класс, являющийся расширением класса `BluetoothDevice`. Позволяет более тесно связать Bluetooth устройство и его RSSI
  
  #### ConnectionActivityDev
  Данный класс реализует механизм подключения к Bluetooth устройству, подобный классу `LightManageActivity`, только для одного устройства. Строит графический интерфейс для прямого взаимодействия с сервисами и характеристиками GATT устойства через текстовые диалоговые окна
  
  #### MultiConnectionActivityDev
  Данный класс реализует механизм подключения к Bluetooth устройству, подобный классу `LightManageActivity`, сразу к нескольким устройствам BLightESP32. Строит единый графический интерфейс для прямого взаимодействия с сервисами и характеристиками GATT сразу всех устройств через текстовые диалоговые окна

</details>

<details>
  <summary><h3>gatt_attr</h3></summary>
  Данный модуль содержит в себе классы для хранения коллекций (словарей) соответствия UUID GATT сервисов, характеристик, дескрипторов и их имен.
  
  #### AllGattServices
  Содержит словарь названий сервисов и соответствующих им UUID
  
  #### AllGattCharacteristics
  Содержит словарь названий характеристик и соответствующих им UUID
  
  #### AllGattDescriptors
  Содержит словарь названий дескрипторов и соответствующих им UUID
  
</details>
  
<details>
  <summary><h3>light_picker</h3></summary>
  Данный модуль содержит в себе реализацию графического интерфейса для выбора светового режима в пользовательском режиме (User mode) работы приложения. Реализация модификацией библиотеки <a href="https://github.com/Madrapps/Pikolo"> Pikolo </a>

</details>

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- ACKNOWLEDGMENTS -->
## Acknowledgments
Приложение разработано в рамках НИР "Разработка механизмов проекцирования процессов жизнедеятельности пользователей в экосистему их цифровых ассистентов" №621308

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[android-studio-shield]: https://img.shields.io/badge/Android%20Studio-000000?style=for-the-badge&logo=androidstudio
[android-studio-url]: https://developer.android.com/studio
[pikolo-shield]: https://img.shields.io/badge/Pikolo-7F52FF?style=for-the-badge
[pikolo-url]: https://github.com/Madrapps/Pikolo
