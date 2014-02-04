OpenGLCamera
============

Android camera using SurfaceTexture

This is a minimalistic example of custom camera preview app that uses SurfaceTexture and uses a matrix transform to change the way it is displayed. 

Here, we show front camera image not as a mirror.

Many details like error checking have been skipped for brevity. In the real world, you will open the camera on a separate EventThread, so that the camera callbacks would not interfere with UI thread.     