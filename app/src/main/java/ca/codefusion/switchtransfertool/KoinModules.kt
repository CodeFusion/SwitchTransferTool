package ca.codefusion.switchtransfertool

import ca.codefusion.switchtransfertool.data.localstorage.LocalRepository
import ca.codefusion.switchtransfertool.data.localstorage.SharedPrefsRepository
import ca.codefusion.switchtransfertool.interactors.GalleryInteractor
import ca.codefusion.switchtransfertool.interactors.NetworkInteractor
import ca.codefusion.switchtransfertool.interactors.SettingsInteractor
import ca.codefusion.switchtransfertool.ui.main.MainViewModel
import ca.codefusion.switchtransfertool.ui.transfer.downloader.DownloadingViewModel
import ca.codefusion.switchtransfertool.ui.transfer.scanner.ScannerViewModel
import ca.codefusion.switchtransfertool.ui.transfer.tutorial.TutorialViewModel
import ca.codefusion.switchtransfertool.utils.TutorialUtil
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

object KoinModules {

    val appModule = module {
        single<LocalRepository> { SharedPrefsRepository(get()) }
        single { TutorialUtil(get(), get()) }
    }

    val interactorModule = module {
        factory { NetworkInteractor() }
        factory { GalleryInteractor() }
        factory { SettingsInteractor() }
    }

    val viewModelModule = module {
        viewModel { SwitchTransferToolViewModel(get()) }
        viewModel { MainViewModel(get(), get(), get(), get(), get()) }
        viewModel { ScannerViewModel(get(), get()) }
        viewModel { DownloadingViewModel(get(), get(), get()) }
        viewModel { TutorialViewModel(get(), get()) }
    }
}