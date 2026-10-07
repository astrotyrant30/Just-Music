# Room database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Media3 ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Keep Data Models and Room Entities
-keep class com.justmusic.app.data.model.** { *; }
-keep class com.justmusic.app.data.db.** { *; }

# Kotlinx Coroutines
-dontwarn kotlinx.coroutines.**
