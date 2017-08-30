# AndroidDemoApp

An android app that demonstrates two important android frameworks:

  * Dagger 2 fast dependency injection: https://github.com/google/dagge

  * Android Bootstrap: https://github.com/Bearded-Hen/Android-Bootstrap


# Async Model

If you look closely at the code you will see that it is inspired by a node.js async model.



For Example inside CameraUtils:

https://github.com/g00dnatur3/AndroidDemoApp/blob/master/app/src/main/java/com/g00dnatur3/camera/CameraUtils.java

You will see almost every method ends with a Handler and Function, ex:

```
public static void openCamera(... final Handler handler, final Function onComplete) {

}
```

This is so that the work can be executed async with the specified handler and it will call the onComplete function when finished... 


Definition of a Function:

```
public interface Function {
    public void call(Object... args);
}
```

You can see a function is a call method with an array of args (Objects).

# Example Async Call

from: https://github.com/g00dnatur3/AndroidDemoApp/blob/master/app/src/main/java/com/g00dnatur3/app/view/fragment/CameraFragment.java

```
Function onComplete = new Function() {
    @Override
    public void call(Object... args) {
        if (args[0] != null) {
            Log.i(TAG, "openCamera - err: " + args[0]);
            cameraDevice = null;
        } else {
            cameraDevice = (CameraDevice) args[1];
            createPreviewSession();
        }
    }
};
CameraUtils.openCamera(getActivity(), cameraManager, cameraId, getBackgroundHandler(), onComplete);
```


# Error Handling of Async


if the args[0] is not null then args[0] is the error & the async call failed.

If the args[0] is null then the async call was success and the args[1..n] represent the return vals of the async call (if any)





Hope that all makes sense :)








