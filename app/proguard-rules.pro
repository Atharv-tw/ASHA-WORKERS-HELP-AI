# Vosk (offline speech recognition, JNI)
-keep class org.vosk.** { *; }
-keep class com.sun.jna.** { *; }
-keep class org.sqlite.** { *; }

# SQLCipher (encrypted database, JNI)
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# Room
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# Our WorkManager worker is instantiated reflectively
-keep class com.asha.worker.ai.work.** { *; }

# Keep Room entities' fields intact
-keep class com.asha.worker.ai.data.** { *; }

# Desktop-only classes referenced by JNA (via Vosk) and Tink (via security-crypto)
# that never run on Android — silence the R8 missing-class errors.
-dontwarn java.awt.**
-dontwarn javax.annotation.**
-dontwarn com.sun.jna.**
