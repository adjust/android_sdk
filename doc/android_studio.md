## Integrate adjust using Android Studio

If you want to use the adjust SDK on your android application using the Android
Studio you have to follow some steps to circumvent the current limitations that
it has. This limitation is being addressed at [google].

The following steps were taken from a [stack overflow] question.

### Import the adjust SDK as a new module

1. In the `Android Studio` menu click `File → New Module`.

    ![][new_module]

2. Select the option `Android Library`. Press `Next`.

    ![][android_library]

3. Fill the form with follow values:

    ```
        Module name: Adjust
        Package name: com.adjust.sdk
        Minimum required SDK: API 8: Android 2.2 (Froyo)
        Theme: None
    ```

    Select the `target SDK` and `Compile with` android SDK options according to
    your project.  Untick the `Create activity` box and leave the rest
    unticked.  Press `Next`.

    ![][form]

4. Make sure that an `Adjust` folder was created in the your `Android Studio`
   project and that the `Adjust → src → main → java → com.adjust.sdk` folder is
   empty.

    ![][empty]

4. Download and extract the latest adjust android SDK from our [releases] page.

5. From the extracted folder, select and copy the files from
   `Adjust/src/com/adjust/sdk/`.

    ![][copy]

6. In your `Android Studio` project. Select the folder `Adjust → src → main →
   java → com.adjust.sdk` and paste.

    ![][paste]

7. Confirm the copy in the popup dialog that appears.

    ![][confirm_copy]

### Add the `Adjust` module to your project

8. In the `Android Studio` menu click `File → Project Structure...`.

    ![][project_structure]

9. Select the `Dependencies` tab of your module and add `Adjust` as a `Module
   dependency`.

    ![][dependencies]

    ![][modules]

10. From this step you can follow the [android sdk] guide from step `4 - Add
    permissions` onwards.

[stack overflow]: http://stackoverflow.com/questions/20310164/how-to-import-eclipse-library-project-from-github-to-android-studio-project
[google]: https://code.google.com/p/android/issues/detail?id=62122
[releases]: https://github.com/adjust/android_sdk/releases
[android sdk]: https://github.com/adjust/android_sdk/blob/master/README.md#4-add-permissions
[new_module]: https://raw.github.com/adjust/sdks/master/Resources/android/android_studio_01_new_module.png
[android_library]: https://raw.github.com/adjust/sdks/master/Resources/android/android_studio_02_android_library.png
[form]: https://raw.github.com/adjust/sdks/master/Resources/android/android_studio_03_form.png
[empty]: https://raw.github.com/adjust/sdks/master/Resources/android/android_studio_04_empty.png
[copy]: https://raw.github.com/adjust/sdks/master/Resources/android/android_studio_05_copy.png
[paste]: https://raw.github.com/adjust/sdks/master/Resources/android/android_studio_06_paste.png
[confirm_copy]: https://raw.github.com/adjust/sdks/master/Resources/android/android_studio_07_confirm_copy.png
[project_structure]: https://raw.github.com/adjust/sdks/master/Resources/android/android_studio_08_project_structure.png
[dependencies]: https://raw.github.com/adjust/adjust_sdk/master/Resources/android/android_studio_09_dependencies.png
[modules]: https://raw.github.com/adjust/adjust_sdk/master/Resources/android/android_studio_10_modules.png
