package com.swordfish.lemuroid.lib.library

import androidx.paging.PagingData
import androidx.paging.filter
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Hides games of systems withdrawn through [SystemAvailability]. The check runs on every emission. */
fun Flow<List<Game>>.withoutDisabledSystems(): Flow<List<Game>> {
    return map { games -> games.filterNot { SystemAvailability.isDisabled(it.systemId) } }
}

/** Paging variant of [withoutDisabledSystems]. */
fun Flow<PagingData<Game>>.pagedWithoutDisabledSystems(): Flow<PagingData<Game>> {
    return map { page -> page.filter { !SystemAvailability.isDisabled(it.systemId) } }
}
