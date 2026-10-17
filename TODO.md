# TODO — fShop

Pendências conhecidas deste plugin (15/10/2026). Riscar quando feito.

## Funcionalidades
- [x] Ligar as lojas à economia (fEconomy) via `EconomyApi` do fAPI.
- [ ] Guardar as lojas: não encontrei persistência das lojas no código; usar o `DatabaseService` partilhado (`Databases.get()` + migração).

## Código
- [ ] O `MessagesManager` lê as mensagens de `getConfig()` (`Mensagens.playeroff`, etc.), mas o plugin não tem `config.yml` nos recursos nem chama `saveDefaultConfig()`: os valores vêm `null`. Criar o `config.yml` e gravá-lo por omissão.
- [ ] `SignShopManager`: criar uma exceção própria (comentário `TODO` no código).
- [ ] `SignShopManager`: mover a lógica marcada para o fAPI (comentário `TODO` no código).

## Qualidade
- [x] Testes automáticos (`ShopOffer`, `ShopTrade`, `SignShopManager`).

## Depois da integração com a economia
- Lojas de jogador: baú de stock ligado à placa, `transfer` para o dono, persistência (tabela `fshop_shops` com localização, dono por UUID, item com data value, quantidade e preços) e reconhecimento depois de reiniciar.
- O item da loja é só o nome na linha 4: perde o data value (cores da lã, poções) e a durabilidade; `PlayerBag` conta só o tipo de item.
- Código morto a decidir: `MessagesManager`, `VariablesManager`, `ItemAddCommand` (referem o plugin antigo).
- `removeColorCodes` do `SignShopManager` devia ir para o fAPI.
- Dependência `sqlite-jdbc` do `build.gradle.kts` já não é usada.
- Se o preço da placa for demasiado alto (ex.: 90 mil milhões), o `deposit` pode estourar o `BIGINT` e a venda dá erro genérico.
- `PlayerBag` compara só o `Material`: ignora data value, encantamentos e durabilidade.
- O `inFlight` pode ficar preso e uma compra pode perder dinheiro se o scheduler recusar uma tarefa enquanto o plugin se desativa.
- `ShopOffer.fromSign` aceita ids numéricos de item via `matchMaterial` e quantidades como `+5`.
- `SignShopManager` (linhas ~104/110) compara o título com `equalsIgnoreCase("loja")` sem normalizar cores (só deteção, não a verificação de permissão).
- `ShopCreateListener` compara o título do dono sem normalizar cores.
- Uma exceção síncrona em `ShopTrade.sell` perde os itens retirados (improvável: o `EconomyApi` devolve futuros).
- Nada obriga a venda <= compra na criação de uma loja do servidor: `C 5 / V 10` permite um ciclo de dinheiro infinito (só admins). Validar em `SignShopImpl.createShop`.
- Se o jogador sair entre o `withdraw` e a entrega na thread principal, o `give` vai para um `Player` offline e os itens perdem-se. Verificar `isOnline()` e reembolsar.
- Teste manual no jogo: clicar numa placa de loja antes do `/login` não faz nada; admins têm de agachar-se (sneak) para partir uma placa de loja, porque o clique esquerdo é cancelado.
