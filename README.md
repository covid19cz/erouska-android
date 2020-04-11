# erouska-android

## [erouska.cz](https://erouska.cz)
## [Download from Google Play](https://play.google.com/store/apps/details?id=cz.covid19cz.erouska)

Read our [**FAQ** (Czech only)](https://erouska.cz/caste-dotazy)

eRouška (_rouška_ = _face mask_ in Czech) helps to fight against COVID-19.

eRouška uses Bluetooth to scan the area around the device for other eRouška users and saves the data of these encounters.

When an eRouška user tests positive for COVID-19, the user is contacted by a healthcare authority and asked to upload the data to create a map of potential secondary infections.

The healthcare authorities then analyse the data and contact the possibly newly infected for further measures (quarantine, testing).

The App is registered with a phone number. The phone numbers are available only for the healthcare authorities.


## How do we protect the user's privacy?

- User can remove all collected data, including the phone number.
- All data are saved locally on the user's device. Data are uploaded only with user's consent after a healthcare authority's request.
- The scanning can be turned off manually at any time.
- The broadcasted _Device ID_ is changed every hour, so a user cannot be tracked with it. (Only our backend has a knowledge of which _Device ID_'s correspond to which phone number.)
- The data are kept on backend for 6 hours, then deleted.
- eRouška is developed open-source from day one.

## Who is developing eRouška?

We are an initiative consisting of people from various Czech IT companies and volunteers called [COVID19CZ](https://covid19cz.cz). We are all unpaid volunteers. 

We are on Slack! [covid19cz.slack.com](covid19cz.slack.com), channel _#erouska_.

The development was subsequently approved by [Czech Ministry of Healthcare](https://www.mzcr.cz/). 

## International cooperation

We are open-source from day one and we will be happy to work with people in other countries if they want to develop a similar app. Contact [David Vávra](mailto:david.vavra@erouska.cz) for technical details.

We got inspired by similar apps in other countries:
- [**OpenTrace** from Singapore](https://github.com/OpenTrace-Community)
- [**ProteGO-app** from Poland](https://github.com/ProteGO-app)
- [**Covid World** from Slovakia](https://github.com/CovidWorld)

## Technical details

- eRouška uses: 
	- Bluetooth Low Energy (BLE)
	- [Firebase Phone Number Authentication](https://firebase.google.com/docs/auth/android/phone-auth) for phone number verification and authentication
	- [Firebase Storage](https://firebase.google.com/docs/storage) for uploading the collected user data in CSV format
	- [Firebase Functions](https://firebase.google.com/docs/functions) for getting the _Device ID_'s to broadcast after registration
	- [Firebase Crashlytics](https://firebase.google.com/docs/crashlytics) for app crash monitoring

- For Android <-> Android Bluetooth connection, the App doesn't need to connect to the other device as the _Device ID_ is broadcasted in the broadcast payload.
- For Android <-> iOS Bluetooth connection, the App needs to connect to the iOS device via GATT to get the _Device ID_.

More details about eRouška:
- [Technical documentation in Wiki](https://github.com/covid19cz/erouska-android/wiki/Technical-documentation)
- [erouska-ios](https://github.com/covid19cz/erouska-ios): iOS source code
- [erouska-firebase](https://github.com/covid19cz/erouska-firebase): Firebase Functions source code
- [erouska-homepage](https://github.com/covid19cz/erouska-homepage): erouska.cz webpage source code

## How to build the app

Clone this repository and import the project into Android Studio.

In a terminal:
`./gradlew assembleDevDebug`

## Contributing
We are happy to accept pull requests! See [Git Workflow](#git-workflow).

We are on Slack! [covid19cz.slack.com](covid19cz.slack.com), channel _#erouska_.

We use a private [Trello](https://trello.com/b/4xN2Eeqv/bug-wf) for bug tracking, contact us on Slack.

## <a name="git-workflow"></a>Git workflow

- Do all work in a fork, then send a pull request to the `develop` branch. 
- Pull requests are merged with `squash commits`.
- Admins merge `develop` to `master` with a `rebase` strategy. This triggers a release build.

## eRouška release process

eRouška uses GitHub Actions. A push to master branch triggers an app build. Then the app is published to [Firebase App Distribution](https://firebase.google.com/docs/app-distribution). 

There are two variants of the App: **DEV** and **PROD**. **PROD** is also built as an App Bundle artefact, that needs to be manually uploaded to Google Play.

Versioning is automatic: major and minor version is in Git, patch is _versionCode_ (a number of commits from the start).
