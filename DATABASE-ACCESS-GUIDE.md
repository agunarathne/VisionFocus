# VisionFocus Database Access Guide

## Problem Summary

The `visionfocus_database.db` file pulled from the device appears corrupted when opened in SQLite viewers with the error:
```
SQLITE_NOTADB: sqlite3 result code 26: file is not a database
```

## Root Cause

**The database is encrypted with SQLCipher** (AES-256 encryption), as implemented in Story 4.2 for security compliance.

### Encryption Details
- **Encryption Library**: SQLCipher (Android)
- **Passphrase Management**: Android Keystore System
- **Key Details**: Located in `EncryptionHelper.kt`
- **Implementation**: See `DatabaseModule.kt` lines 89-90

The database file header shows encrypted data (e.g., `�9a������n)e���`) instead of the standard SQLite header (`SQLite format 3`).

## Why the Original Pull Method Failed

The original task command used:
```powershell
adb shell 'run-as com.visionfocus cat databases/visionfocus_database' > visionfocus_database.db
```

This corrupts binary data because PowerShell's `>` operator applies encoding transformations. Even after fixing this with proper `adb pull`, the database remains unreadable because it's encrypted.

## Solutions

### Option 1: Export Plaintext Data (Recommended for Debugging)

Add an export function to the app that writes recognition history to a readable format:

```kotlin
// Add to RecognitionHistoryRepository or create ExportHelper
suspend fun exportToJson(context: Context): File {
    val records = getAllRecognitions() // Get all data
    val json = Json.encodeToString(records)
    val file = File(context.getExternalFilesDir(null), "export.json")
    file.writeText(json)
    return file
}
```

Then pull the JSON file:
```powershell
adb pull /sdcard/Android/data/com.visionfocus/files/export.json
```

### Option 2: View Database On-Device

Use an in-app database inspector:
- **Android Studio Database Inspector**: View > Tool Windows > App Inspection > Database Inspector
- This automatically handles encryption and shows live data while debugging

### Option 3: Decrypt Locally (Advanced)

You would need:
1. The encryption passphrase (stored in Android Keystore - **cannot be extracted**)
2. SQLCipher desktop tools
3. Since the key is hardware-backed and non-exportable, this option is **not feasible**

### Option 4: Unencrypted Build Variant (Development Only)

Create a debug build variant without encryption:

```kotlin
// In DatabaseModule.kt - Add debug flag
@Provides
@Singleton
fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
    return if (BuildConfig.DEBUG && !USE_ENCRYPTION) {
        // Unencrypted for development/testing
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(MIGRATION_1_2)
            .build()
    } else {
        // Encrypted for production
        val passphrase = EncryptionHelper.getDatabasePassphrase(context)
        val factory = SupportFactory(passphrase)
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .openHelperFactory(factory)
            .addMigrations(MIGRATION_1_2)
            .build()
    }
}
```

## Fixed Database Pull Script

While the database will still be encrypted, this properly pulls the binary file:

```powershell
# Copy to accessible location
adb shell "run-as com.visionfocus cp databases/visionfocus_database /sdcard/Download/vf_temp.db"

# Pull using adb pull (preserves binary data)
adb pull /sdcard/Download/vf_temp.db visionfocus_database.db

# Clean up
adb shell "rm /sdcard/Download/vf_temp.db"
```

**Note**: The pulled file will still be encrypted and unreadable without the passphrase.

## Viewing Database Contents

### Recommended: Android Studio Database Inspector

1. Connect your device via ADB
2. Open Android Studio
3. Go to `View > Tool Windows > App Inspection`
4. Select `Database Inspector` tab
5. Select your app process (`com.visionfocus`)
6. View tables: `recognition_history`, `saved_locations`

**Advantages**:
- ✓ Works with encrypted databases
- ✓ Real-time data viewing
- ✓ Can run queries directly
- ✓ No code changes needed

### Alternative: LogCat Monitoring

Use the existing monitoring tasks:
```powershell
# Monitor live database inserts
Run Task: "Monitor Database (Live)"

# View database statistics
Run Task: "Database Stats"
```

## Database Schema (Version 2)

### RecognitionHistoryEntity
| Column | Type | Description |
|--------|------|-------------|
| id | Int (PK) | Auto-generated ID |
| objectLabel | String | Recognized object name |
| category | String | Object category |
| confidence | Float | Recognition confidence (0.0-1.0) |
| timestamp | Long | Unix timestamp (ms) |
| verbosityMode | String | "brief"/"standard"/"detailed" |
| detailText | String | Full description text |

### Index
- `idx_recognition_timestamp` on `timestamp` column

## Security Notes

- ✅ Database encryption is **working as designed**
- ✅ Encryption prevents unauthorized access to recognition history
- ✅ Android Keystore ensures passphrase cannot be extracted
- ⚠️ This makes offline database inspection intentionally difficult
- ℹ️ For testing/debugging, use Android Studio Database Inspector or export utilities

## Conclusion

The "corrupted" database is actually properly encrypted. Use Android Studio Database Inspector for viewing or implement data export functionality for offline analysis.
