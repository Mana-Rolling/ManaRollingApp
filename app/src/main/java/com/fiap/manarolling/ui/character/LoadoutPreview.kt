package com.fiap.manarolling.ui.character

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fiap.manarolling.model.ClassPresets
import com.fiap.manarolling.ui.LoadoutCard

/**
 * Pré-visualiza o kit (arma + habilidade) dado o nome da classe.
 * Use este composable dentro da CreateCharacterScreen, CharacterDetails e Sessão.
 *
 * Exemplo de uso:
 *   LoadoutPreview(clazz = state.clazz, modifier = Modifier.padding(16.dp))
 */
@Composable
fun LoadoutPreview(
    clazz: String,
    modifier: Modifier = Modifier
) {
    val loadout = ClassPresets.loadoutFor(clazz)

    if (loadout == null) {
        OutlinedCard(
            modifier = modifier.fillMaxWidth()
        ) {
            Text(
                text = "Selecione uma classe para ver a arma e a habilidade.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    LoadoutCard(
        loadout = loadout,
        modifier = modifier.fillMaxWidth(),
        title = "Arma & Habilidade (${clazz})"
    )
}
