import android.Keys._

android.Plugin.androidBuild

name := "uow-borderauth"

scalaVersion := "2.10.3"

proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize")

libraryDependencies ++= Seq(
  "org.scaloid" %% "scaloid" % "3.0-8",
  "com.github.kevinsawicki" % "http-request" % "5.5"
)

proguardCache in Android += ProguardCache("org.scaloid") % "org.scaloid" %% "scaloid"

scalacOptions in Compile += "-feature"

run <<= run in Android

install <<= install in Android
