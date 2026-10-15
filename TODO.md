# TODO — fShop

Pendências conhecidas deste plugin (15/10/2026). Riscar quando feito.

## Funcionalidades
- [ ] Ligar as lojas à economia (fEconomy). Depende de expor o `EconomyService` a outros plugins (ver o `TODO.md` do fEconomy) e de chamar `ensureAccount` antes de depositar.
- [ ] Guardar as lojas: não encontrei persistência das lojas no código; usar o `DatabaseService` partilhado (`Databases.get()` + migração).

## Código
- [ ] O `MessagesManager` lê as mensagens de `getConfig()` (`Mensagens.playeroff`, etc.), mas o plugin não tem `config.yml` nos recursos nem chama `saveDefaultConfig()`: os valores vêm `null`. Criar o `config.yml` e gravá-lo por omissão.
- [ ] `SignShopManager`: criar uma exceção própria (comentário `TODO` no código).
- [ ] `SignShopManager`: mover a lógica marcada para o fAPI (comentário `TODO` no código).

## Qualidade
- [ ] O fShop não tem testes automáticos.
