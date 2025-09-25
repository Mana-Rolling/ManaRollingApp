package com.fiap.manarolling.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fiap.manarolling.model.ClassPresets

@Composable
fun LoadoutCard(
    loadout: ClassPresets.Loadout,
    modifier: Modifier = Modifier,
    title: String = "Arma & Habilidade"
) {
    val context = LocalContext.current

    val weaponResId = remember(loadout.weaponImageRes) {
        context.resources.getIdentifier(loadout.weaponImageRes, "drawable", context.packageName)
    }
    val abilityResId = remember(loadout.abilityImageRes) {
        context.resources.getIdentifier(loadout.abilityImageRes, "drawable", context.packageName)
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Arma
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Arma", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (weaponResId != 0) {
                            Image(
                                painter = painterResource(id = weaponResId),
                                contentDescription = "Arma",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Column {
                            Text(loadout.weaponName, style = MaterialTheme.typography.bodyLarge)
                            Text("Dano: ${loadout.weaponDamage}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Habilidade
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Habilidade", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (abilityResId != 0) {
                            Image(
                                painter = painterResource(id = abilityResId),
                                contentDescription = "Habilidade",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Column {
                            Text(loadout.abilityName, style = MaterialTheme.typography.bodyLarge)
                            val effect = if (loadout.healing) "Cura: ${loadout.abilityPower}" else "Dano: ${loadout.abilityPower}"
                            Text(effect, style = MaterialTheme.typography.bodySmall)
                            AssistChip(onClick = {}, enabled = false, label = { Text("Mana: ${loadout.manaCost}") })
                        }
                    }
                }
            }
        }
    }
}
