# erouska-android

[<img src="https://lh3.googleusercontent.com/cjsqrWQKJQp9RFO7-hJ9AfpKzbUb_Y84vXfjlP0iRHBvladwAfXih984olktDhPnFqyZ0nu9A5jvFwOEQPXzv7hr3ce3QVsLN8kQ2Ao=s0">](https://play.google.com/store/apps/details?id=cz.covid19cz.erouska)

Read our [**FAQ** (Czech only)](https://erouska.cz/caste-dotazy)

eRouška (_rouška_ = _face mask_ in Czech) helps to fight against COVID-19.

eRouška uses Bluetooth to scan the area around the device for other eRouška users and saves the data of these encounters.

It's the only app in Czechia authorized to use Exposure Notifications API from Apple/Google.

## Who is developing eRouška?

Starting with version 2.0, the eRouška application is developed by the Ministry of Health in collaboration with the National Agency for Communication and Information Technologies ([NAKIT](https://nakit.cz/)). Earlier versions of eRouška application were developed by a team of volunteers from the [COVID19CZ](https://covid19cz.cz) community. Most of original eRouška developers continue to work on newer versions in the NAKIT team.

## International cooperation

We are open-source from day one and we will be happy to work with people in other countries if they want to develop a similar app. Contact [David Vávra](mailto:david.vavra@erouska.cz) for technical details.

## Building the App from the source code

Clone this repository and import the project into Android Studio. Make sure you have JDK 8.

Run:
`./gradlew assembleDevDebug`

## Contributing
We are happy to accept pull requests! See [Git Workflow](#git-workflow).

If you want to become a more permanent part of the team, join [our Slack](covid19cz.slack.com), channel _#erouska_.

## Translations

Help us translate to your language or if you see a problem with translation, fix it. Our translation is open to volunteers [at OneSky](https://covid19cz.oneskyapp.com/).

## <a name="git-workflow"></a>Git workflow

- Work in a fork then send a pull request to the `develop` branch. 
- Pull requests are merged with `squash commits`.
- Admins rebase `develop` to `master` using the script below. This triggers a release build.

## eRouška release process

eRouška uses GitHub Actions. A push to master branch triggers an App build. Then the App is published to [Firebase App Distribution](https://firebase.google.com/docs/app-distribution). 

There are two variants of the App: **DEV** and **PROD**. **PROD** is also built as an App Bundle artefact, that needs to be manually uploaded to Google Play.

Versioning is automatic: major and minor version is in Git, patch is _versionCode_ (a number of commits from the start).

Release is done by executing the release.sh script. Right click it on Android Studio and hit Run 'release.sh' or execute via command line.
If it fails, it fails. Most likely your master has different history from origin. That should never be the case, so you should fix it.
