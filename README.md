# Circle progress bar

A Circle progress animated.<br>
## Example

![screenshot](./images/WithDone.gif "Sample")

## Usage
``` xml
 <com.cleveroad.progresscircle.ProgressCircle
         android:id="@+id/progressView"
         android:layout_width="200dp"
         android:layout_height="200dp"
         android:layout_centerHorizontal="true"
         android:layout_centerVertical="true"
         android:padding="0dp"
         app:showDone="false"
         app:maxProgress="100"
         app:startColor="@color/colorStart"
         app:endColor="@color/colorEnd"
         app:startAngle="-90"
         app:thicknessLine="12dp" />
```
## Usage
Remember put this for custom attribute usage

``` java
xmlns:app="http://schemas.android.com/apk/res-auto"
```

Custom attributes
------------
* Starting rotation angle

``` java
 app:startAngle="-90"

```
* Maximun value for progress

``` java
 app:maxProgress="100"
```
* Color for the gradient

``` java
 app:startColor="@color/colorStart"
 app:endColor="@color/colorEnd"
```
* Color for the done

``` java
 app:doneColor="#6effffff"
```
* Line width progress bar

``` java
  app:thicknessLine="12dp"
```
* If set "false" not shown done

``` java
    app:showDone="false"
```
![screenshot](./images/NowithDone.gif "Sample")

End Listener
------------
``` java
 progressCircle.setListener(new ProgressCircleListener() {
            @Override
            public void endAnimation() {
                
            }
        });

```
License
-------

Copyright 2015 Adrián García Lomas

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
