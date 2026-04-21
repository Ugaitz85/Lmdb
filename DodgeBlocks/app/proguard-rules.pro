# Reglas mínimas para apps Compose (mantener @Composable metadata no suele ser necesario,
# pero esto evita problemas con reflection de tooling en builds optimizados).
-keep class kotlin.Metadata { *; }

# DataStore (Preferences) suele funcionar sin reglas extra; mantener por seguridad.
-dontwarn kotlinx.coroutines.**
-dontwarn androidx.datastore.**
