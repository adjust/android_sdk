## Adjust 스토어 & 사전 설치 트래커 툴

Adjust의 스토어 & 사전 설치 트래커 툴(`adjust-dtt`)은 APK 파일에 Adjust의 사전 설치 트래커 정보를 넣을 수 있도록 해주는 Ruby 명령어 툴입니다. 이후 Adjust SDK는 이 정보를 읽고, 특정 Adjust 트래커에 개별 사전 설치 앱 유저를 연결할 수 있도록 해줍니다.

`adjust-dtt` 툴을 실행하려면 Linux / Mac OS X 머신에 3개의 툴이 설치되어 있어야 합니다.

1. `ruby` – `adjust-dtt` 툴을 실행하기 위해 사용
2. `apktool` – APK 언패킹 & 리패킹을 위해 사용
3. `jarsigner` – APK 서명 목적으로 사용

Ruby는 Mac OS X에 기본으로 설치되어 있습니다. 사용자의 Linux에 Ruby가 설치되어 있지 않은 경우, Linux 배포 버전별 설치 방법(https://www.ruby-lang.org/en/documentation/installation/) 을 확인하시기 바랍니다.

apktool 설치 방법은 https://ibotpeaches.github.io/Apktool/install/ 에서 확인하시기 바랍니다.

머신의 Mac OS X 설정에 대해 `brew` 패키지 매니저(http://brew.sh/) 가 있는 경우, 다음을 실행하여 apktool을 간편하게 설치할 수 있습니다.

```
brew install apktool
```

또는, Java 개발 키트(JDK)에 포함된 `jarsigner` 툴을 사용할 수 있으며, 해당 키트를 머신에 설치해야 합니다. JDK(예: 1.8 버전)가 Mac OS X에 설치되면 위치는 다음과 같습니다.

```
/Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/
```

JDK 설치는 `bin` 폴더를 포함하며, 이는 다음의 경로에서 찾을 수 있습니다. (위와 동일한 설치 경로를 사용한 경우에만 해당)

```
/Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home/bin/
```

해당 폴더 안에서 `jarsigner` 툴을 찾을 수 있으며, 이는 `adjust-dtt` 툴을 사용하기 위해 필요합니다.

`adjust-dtt` 툴은 `apktool`과 `jarsigner` 툴의 경로가 시스템 `PATH` 변수에 추가되었다고 가정할 것입니다. 만약 이 경우에 해당하지 않는다면, `PATH` 변수에 두 툴을 포함하는 폴더를 추가해야 합니다.

**참고**: 위의 추천 방법(공식 설치 가이드 또는 `brew` 메서드)에 따라 `apktool` 을 추가하면 터미널 어디에서나(`PATH` 변수에 자동으로 경로가 추가됨) `apktool`을 실행할 수 있으나, `apktool`이 `PATH` 변수에 속하지 않는 위치에 추가된 경우에는 `PATH` 변수에 이를 포함하는 폴더를 추가해야 합니다.

`apktool` (1)과 `jarsigner` (2)의 위치를 다음과 같이 가정해보겠습니다.

1. /User/dummy/some/random/folder/
2. /Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home/bin/

`PATH` 변수에 이 두 위치를 추가하려면 `.bash_profile` 파일을 수정해야 합니다. 이는 일반적으로 `$HOME` 디렉토리의 루트에 있습니다.

```
$HOME/.bash_profile
```

원하는 편집 프로그램에서 이를 연 뒤 다음을 끝에 추가합니다.

```
export PATH="/Users/dummy/some/random/folder:/Library/Java/JavaVirtualMachines/jdk 1.8.0_102.jdk/Contents/Home/bin:$PATH"
```

`apktool`이 자동으로 `PATH`에 존재(예: `brew`를 통한 설치에 해당)하는 경우에는 `PATH` 변수에 경로를 별도로 추가하지 않아도 됩니다. 대신(위의 예와 같이), 위 라인이 아니라 다음의 라인을 추가합니다.

```
export PATH="/Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home/bin: $PATH"
```

변경 내용을 저장한 뒤 다음의 명령어를 실행하여 즉시 변경을 적용합니다.

```
source $HOME/.bash_profile
```

**참고**: 본 튜토리얼은 사용자가 배시 쉘(`bash` shell)을 사용한다고 가정합니다. 그 외의 쉘(예: `zsh`)을 사용하는 경우 쉘 설정에 따라 위 단계를 수행하시기 바랍니다.

이제 모든 설정이 완료되어 `adjust-dtt` 툴을 사용할 수 있습니다.

사용자가 배포하고자 하는 APK는 서명됩니다. APK의 "비서명" 버전을 생성한 경우에도 Android Studio에서 머신에 존재하는 기본값 디버그 서명 키스토어를 통해 서명이 이뤄질 것입니다. 이 경우 가장 가능성 높은 위치는 다음과 같습니다.

```
$HOME/.android/debug.keystore
```

서명된 APK를 Android Studio로부터 내보내기 하는 경우라면, 맞춤 서명 키스토어 파일로 APK를 서명합니다.

기본값 디버그 키스토어 파일이나 맞춤 키스토어 파일에 액세스할 수 있어야 `adjust-dtt` 툴을 사용할 수 있습니다. `apktool`은 APK를 언패킹하기 위해 사용된 이후 APK의 모든 서명을 완전히 삭제하기 때문에, 편집 이후 APK가 리패킹 될 때 서명이 재활성화되지 않기 때문입니다. 바로 이러한 이유로 `jarsigner` 툴을 실행하여 서명 정보를 전송해야만 APK를 재서명하여 Android 기기에서 전달 및 사용될 수 있습니다.

이제 마지막으로 남은 단계는 Adjust 대시보드에서 사전 설치앱의 기본값 트래커로 사용할 트래커를 생성하고 토큰을 얻는 작업입니다.

이 모든 정보를 사용하여 `adjust-config.yaml` 구성 파일을 생성하고, 앱을 게시하고자 하는 각 스토어에 대한 설정을 채웁니다. 이는 사전 설치 트래커 설정을 필요로 합니다.

아래의 예시는 `adjust-config.yaml` 파일이 **store_1**, **store_2**, **store_3**라는 가상의 스토어에 대해 APK를 빌드하고 싶다고 가정하는 경우입니다.

```yaml
apk_path: /Users/uerceg/Desktop/apk/example-release.apk
keystore_path: /Users/uerceg/Desktop/apk/mykeystore.jks
keystore_pass: mykeystorepass
keystore_alias: mykeystorealias
stores:
    store_1:
        default_tracker: abc123
    store_2:
        default_tracker: abc456
    store_3:
        default_tracker: abc789
```

다음의 파라미터를 설정할 수 있습니다.

1. `apk_path` = APK 파일 전체 경로 (글로벌 및 스토어별)
2. `keystore_path` = APK 서명을 위해 사용한 키스토어 파일 전체 경로 (글로벌 및 스토어별)
3. `keystore_pass` = 키스토어 서명 암호 (글로벌 및 스토어별)
4. `keystore_alias` = 키스토어 알리아스(alias) (글로벌 및 스토어별)
5. `default_tracker` = 사전 설치된 Adjust 트래커 토큰 (스토어별)

첫 4개의 파라미터는 해당 파라미터를 정의하지 않는 모든 스토어에 동일 설정을 적용하고자 하는 경우 루트에서 글로벌로 정의될 수 있습니다. 파라미터를 글로벌과 스토어에 대해 정의하면, 특정 스토어 파라미터가 APK 생성에 사용될 것입니다. 예를 들어 구성 파일은 다음과 같은 모습입니다.

```yaml
apk_path: /Users/uerceg/Desktop/apk/example-release.apk
keystore_path: /Users/uerceg/Desktop/apk/mykeystore.jks
keystore_pass: mykeystorepass
keystore_alias: mykeystorealias
stores:
    store_1:
        default_tracker: abc123
        keystore_path: /Users/uerceg/Desktop/apk/differentkeystore.jks
        keystore_pass: differentkeystorepass
        keystore_alias: differentkeystorealias
    store_2:
        default_tracker: abc456
    store_3:
        default_tracker: abc789
```

이 경우 `adjust-dtt` 툴은 `differentkeystore.jks`, `differentkeystorepass`, `differentkeystorealias`를 `store_1`을 위한 APK를 생성할 때 사용합니다. **스토어** 파라미터는 반드시 루트에 있어야 하며, 그 안에서 원하는 모든 스토어를 정의할 수 있습니다. 변경된 APK 파일이 각 스토어에 대해 생성됩니다.

`adjust-config.yaml` 파일이 적절하게 구성되면, `adjust-dtt` 툴을 다음과 같이 실행해야 합니다.

```
adjust-dtt adjust-config.yaml
```

**참고**: `adjust-dtt` 툴을 실행할 수 없는 경우, 실행 권한이 있는지 우선 확인하고, 권한이 없는 경우에는 다음을 실행하시기 바랍니다.

```
chmod +x adjust-dtt
```

이후 `adjust-dtt` 툴을 다시 실행합니다.

`adjust-dtt` 툴 실행 시, 다음이 발생할 것입니다.

1. 앱의 APK 파일이 언패킹되고, APK 이름을 가진 폴더가 APK 파일이 위치한 폴더에 생성됩니다.
2. 에셋 폴더를 검색하고, 에셋 폴더 내에 `adjust_config.properties` 파일(기본값 트래커 정보를 포함)을 찾습니다.
3. 해당 에셋 폴더 또는 `adjust_config.properties` 파일이 존재하지 않는다면, 자동 생성되고, `default_tracker` 파라미터로 전송된 트래커는 구성 파일에 쓰입니다.
4. APK가 이미 `adjust_config.properties` 파일을 포함하고 있으나, 기본값 트래커 값이 함께 쓰여있지 않은 경우 `default_tracker` 파라미터 값이 파일에 쓰입니다. 구성 파일이 이미 기본값 트래커 값을 포함한 경우, 기존 값이 명령어에 전달된 값과 비교된 뒤, 서로 값이 다른 경우에는 사용자가 명령어로 전송한 값이 사용됩니다.
5. APK가 리패킹 됩니다.
6. 사용자가 `keystore_path`, `keystore_pass`, `keystore_alias` 파라미터 값으로 전송한 정보를 통해 APK가 서명됩니다.
7. 이제 모든 과정이 끝났습니다. 생성된 APK는 원래의 이름과 동일한 이름에 `_[store_name]` 접미사가 추가된 이름을 갖게 됩니다. 위 예시에서는 `example-release_store_1.apk`, `example- release_store_2.apk`, `example-release_store_3.apk`라는 3개의 APK 파일이 생성될 것입니다. 이는 배포 전에 원하는 이름으로 변경 가능합니다.

본 툴에 관한 문의 및 지원 요청은 support@adjust.com으로 연락주시기 바랍니다.
