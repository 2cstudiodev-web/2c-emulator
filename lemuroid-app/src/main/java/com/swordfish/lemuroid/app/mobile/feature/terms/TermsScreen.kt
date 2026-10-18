package com.swordfish.lemuroid.app.mobile.feature.terms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R

private val SECTIONS =
    listOf(
        R.string.terms_s1_title to R.string.terms_s1_body,
        R.string.terms_s2_title to R.string.terms_s2_body,
        R.string.terms_s3_title to R.string.terms_s3_body,
        R.string.terms_s4_title to R.string.terms_s4_body,
        R.string.terms_s5_title to R.string.terms_s5_body,
        R.string.terms_s6_title to R.string.terms_s6_body,
        R.string.terms_s7_title to R.string.terms_s7_body,
        R.string.terms_s8_title to R.string.terms_s8_body,
    )

/** The terms text, scrollable. Used both inside settings and by [TermsAcceptanceScreen]. */
@Composable
fun TermsContent(
    modifier: Modifier = Modifier,
    showIntro: Boolean = false,
) {
    val sourceUrl = stringResource(R.string.terms_source_url)
    val contactEmail = stringResource(R.string.terms_contact_email)

    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        if (showIntro) {
            Text(text = stringResource(R.string.terms_intro), style = MaterialTheme.typography.bodyLarge)
        }
        SECTIONS.forEachIndexed { index, (titleId, bodyId) ->
            if (showIntro || index > 0) {
                Spacer(modifier = Modifier.height(20.dp))
            }
            Text(text = stringResource(titleId), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            val body =
                when (bodyId) {
                    R.string.terms_s4_body -> stringResource(bodyId, sourceUrl)
                    R.string.terms_s8_body -> stringResource(bodyId, contactEmail)
                    else -> stringResource(bodyId)
                }
            Text(text = body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Shown on first launch, and again whenever [TermsOfUse.VERSION] increases. */
@Composable
fun TermsAcceptanceScreen(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            Text(
                text = stringResource(R.string.terms_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp),
            )
            TermsContent(modifier = Modifier.weight(1f), showIntro = true)
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            ) {
                OutlinedButton(onClick = onDecline) {
                    Text(text = stringResource(R.string.terms_decline))
                }
                Button(onClick = onAccept) {
                    Text(text = stringResource(R.string.terms_accept))
                }
            }
        }
    }
}
