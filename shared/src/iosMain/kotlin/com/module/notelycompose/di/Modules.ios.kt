package com.module.notelycompose.di


import com.module.notelycompose.attachment.AttachmentOpener
import com.module.notelycompose.attachment.AttachmentPicker
import com.module.notelycompose.attachment.ClipboardImageReader
import com.module.notelycompose.audio.domain.AudioRecorderInteractor
import com.module.notelycompose.audio.domain.AudioRecorderInteractorImpl
import com.module.notelycompose.database.NoteDatabase
import com.module.notelycompose.export.domain.ExportSelectionInteractor
import com.module.notelycompose.export.domain.ExportSelectionInteractorImpl
import com.module.notelycompose.platform.BrowserLauncher
import com.module.notelycompose.platform.Downloader
import com.module.notelycompose.platform.IOSPlatform
import com.module.notelycompose.platform.Platform
import com.module.notelycompose.platform.PlatformAudioPlayer
import com.module.notelycompose.platform.PlatformUtils
import com.module.notelycompose.platform.SecureKeyStore
import com.module.notelycompose.platform.Transcriber
import com.module.notelycompose.platform.dataStore
import com.module.notelycompose.platform.pdf.IOSPdfGenerator
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSBundle


actual val platformModule = module {

    single<Platform> { IOSPlatform() }
    single { PlatformUtils(get()) }
    single { BrowserLauncher() }
    single { dataStore() }
    single { IOSPdfGenerator() }
    single { AttachmentPicker() }
    single { AttachmentOpener() }
    single { ClipboardImageReader() }

    single<SqlDriver> {
        NativeSqliteDriver(NoteDatabase.Schema, "notes.db")
    }

    single<String>(qualifier = named("AppVersion")) {
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
            ?: "Unknown"
    }


    single { PlatformAudioPlayer() }

    single { Downloader() }

    single { Transcriber() }

    single { SecureKeyStore() }

    // domain
    single<AudioRecorderInteractor> { AudioRecorderInteractorImpl(get(), get()) }
    single<ExportSelectionInteractor> { ExportSelectionInteractorImpl() }
}