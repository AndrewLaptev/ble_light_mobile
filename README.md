<div id="top"></div>


<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/AndrewLaptev/ble_light_mobile">
    <img src="docs/images/logo.png" alt="Logo" width="125" height="125">
  </a>

<h3 align="center">BLight</h3>

  <p align="center">
    Software module for localization of owners of digital assistants according to mobile device data
  </p>
</div>


<!-- ABOUT THE PROJECT -->
## About

This mobile application is designed for manual wireless control of several nearby smart luminaires at once in a certain area of action, which is set by the user. The ESP 32 microcontroller with firmware [BLightESP32](https://github.com/AndrewLaptev/ble_light_esp32) is used as the control controller of the luminaire. The control is carried out by changing the color temperature of the luminaires, as well as their brightness.

In the future, it is possible to develop the application towards automating the selection of the light mode for the user. This can be done, for example, through API interaction with the user's digital profile, which can transmit to the application the necessary data about switching light modes that are currently most suitable for the user

### Built With
* [![Android Studio][android-studio-shield]][android-studio-url]
* [![Pikolo][pikolo-shield]][pikolo-url]

### Documentation
The application documentation is available in the [GitHub wiki](https://github.com/AndrewLaptev/ble_light_mobile/wiki) of this repository. There you can find instructions for installing the application from the source codes or from the .apk file, using and configuring the use, as well as all technical information about the project.

<!-- ACKNOWLEDGMENTS -->
## Acknowledgments
The application was developed within the framework of the research project "Development of mechanisms for designing the processes of users' vital activity into the ecosystem of their digital assistants" No. 621308

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[android-studio-shield]: https://img.shields.io/badge/Android%20Studio-000000?style=for-the-badge&logo=androidstudio
[android-studio-url]: https://developer.android.com/studio
[pikolo-shield]: https://img.shields.io/badge/Pikolo-7F52FF?style=for-the-badge
[pikolo-url]: https://github.com/Madrapps/Pikolo
