# Vosk
-keep class org.vosk.** { *; }
-keep class com.sun.jna.** { *; }
-keep class org.sqlite.** { *; }

# Room
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
