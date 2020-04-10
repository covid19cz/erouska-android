# erouska-android

Erouška ("rouška" means "face mask" in Czech) is an app which helps to fight against COVID-19. When turned on, it scans other users of the app via Bluetooth and saves this data locally. When some user tests positive for COVID-19, he/she is asked to upload their data. Then healtcare authorities can see phone numbers of users of the app which were in close contact with the infected and can call them about further measures.

Read more at [our website](https://erouska.cz).

[Download from Google Play](https://play.google.com/store/apps/details?id=cz.covid19cz.erouska)

## How do we protect user's privacy?
- All data is saved only locally, they are uploaded only with user's consent
- User can always turn the scanning off
- We change broadcasted ID every hour. Nobody can track you using this ID (only our backend has knowledge which ID corresponds to which phone number)
- We delete data on backend after 6 hours from upload
- This app is open-source from the start, so anyone can check our claims are true

Read more at [our FAQ](https://erouska.cz/faq)

## International cooperation

We got inspired by similar apps in other countries:
- [Singapur](https://github.com/OpenTrace-Community)
- [Poland](https://github.com/ProteGO-app)
- [Slovakia](https://github.com/CovidWorld)

We are open-source from day one and we will be happy to work with people in other countries, if they want to launch similar app. Contact [David Vávra](mailto:david.vavra@erouska.cz) for technical details.

## Technical details

- Bluetooth Low Energy
- For Android-Android, we don't connect to devices, ID is only broadcasted in broadcast payload
- For Android-iOS, we need to connect to the iOS device via GATT to get the ID
- We use [Firebase Phone Number Authentication](https://firebase.google.com/docs/auth/android/phone-auth) for verifying phone numbers and authentication
- We use [Firebase Storage](https://firebase.google.com/docs/storage) for uploading user data in CSV format
- We use [Firebase Functions](https://firebase.google.com/docs/functions) for getting the IDs to broadcast after registration
- We use [Firebase Crashlytics](https://firebase.google.com/docs/crashlytics) for monitoring crashes from the app

## Who is behind the app?

We are initiative consisting from people from various Czech IT companies called [COVID19CZ](https://covid19cz.cz). We are all unpaid volunteers. We got backing from [Czech Ministry of Healthcare](https://www.mzcr.cz/).  

## Contributing

We are happy to accept pull requests. If you want to help more, discuss with us etc., please join [COVID19CZ](https://covid19cz.cz) Slack (channel #erouska), internally we use [Trello](https://trello.com/b/4xN2Eeqv/bug-wf) for bugs.

## Git flow

Do all work in feature branches or in forks, then send pull requests to `develop` branch. Pull requests are merged with `squash commits`. Only admins merge `develop` to `master` with `rebase` strategy, which triggers the release build.

## How to build the app

It should be easy - just checkout the project and import it to Android Studio.

On the command line:
`./gradlew assembleDevDebug`

## Releasing

We have CI (GitHub Actions) for that. With any push to master branch, the app is built and published to [Firebase App Distribution](https://firebase.google.com/docs/app-distribution). We have two variants of the app: DEV and PROD. PROD app is also built as an App Bundle artefact, which needs to be manually uploaded to Google Play.

Versioning is automatic: major and minor version is in Git, patch is versionCode, which is number of commits from the start.
