# TODO — fShop

Pendências conhecidas deste plugin (15/10/2026). Riscar quando feito.

## Funcionalidades
- [x] Ligar as lojas à economia (fEconomy) via `EconomyApi` do fAPI.
- [x] Guardar o item de cada loja do servidor (tabela fshop_items, cache em memória).

## Código
- [ ] O `MessagesManager` lê as mensagens de `getConfig()` (`Mensagens.playeroff`, etc.), mas o plugin não tem `config.yml` nos recursos nem chama `saveDefaultConfig()`: os valores vêm `null`. Criar o `config.yml` e gravá-lo por omissão.
- [ ] `SignShopManager`: criar uma exceção própria (comentário `TODO` no código).
- [ ] `SignShopManager`: mover a lógica marcada para o fAPI (comentário `TODO` no código).

## Qualidade
- [x] Testes automáticos (`ShopOffer`, `ShopTrade`, `SignShopManager`).

## Depois da integração com a economia
- Lojas de jogador: baú de stock ligado à placa, `transfer` para o dono, persistência (tabela `fshop_shops` com localização, dono por UUID, item com data value, quantidade e preços) e reconhecimento depois de reiniciar.
- Código morto a decidir: `MessagesManager`, `VariablesManager`, `ItemAddCommand` (referem o plugin antigo).
- `removeColorCodes` do `SignShopManager` devia ir para o fAPI.
- Dependência `sqlite-jdbc` do `build.gradle.kts` já não é usada.
- Se o preço da placa for demasiado alto (ex.: 90 mil milhões), o `deposit` pode estourar o `BIGINT` e a venda dá erro genérico.
- Placas apagadas por outros meios que não partir (física, explosões, `/setblock`) deixam uma linha órfã em `fshop_items` (inofensiva; é limpa quando se recria uma placa no mesmo sítio).
- Linhas de `fshop_items` que não descodificam são ignoradas em silêncio no arranque (registar).
- Um item definido gasto/danificado é entregue gasto (a venda ignora durabilidade, a compra não a repõe).
- Os itens das lojas de jogador (baú) continuam por fazer e vão reutilizar a tabela `fshop_items`.
- Corrida put-depois-remove: gravar e apagar a mesma chave em seguida correm em threads diferentes do pool e podem trocar de ordem (encadear futuros por chave resolvia).
- `compileOnly sqlite-jdbc` no `build.gradle.kts` pode já não ser necessário (não removido).
- Lojas antigas sem registo em `fshop_items` passam a trocar um item simples (`new ItemStack(material)`) com comparação estrita, por isso já não aceitam itens encantados, renomeados ou com data value diferente de 0 (antes comparavam só o Material); para as ter completas o admin tem de recriar a placa e voltar a definir o item.
- Teste manual no jogo: espada encantada e renomeada definida na placa, comprar dá a espada igual, vender uma espada simples recusa, reiniciar o servidor mantém o item.
- O `inFlight` pode ficar preso e uma compra pode perder dinheiro se o scheduler recusar uma tarefa enquanto o plugin se desativa.
- `ShopOffer.fromSign` aceita ids numéricos de item via `matchMaterial` e quantidades como `+5`.
- `SignShopManager` (linhas ~104/110) compara o título com `equalsIgnoreCase("loja")` sem normalizar cores (só deteção, não a verificação de permissão).
- `ShopCreateListener` compara o título do dono sem normalizar cores.
- Uma exceção síncrona em `ShopTrade.sell` perde os itens retirados (improvável: o `EconomyApi` devolve futuros).
- Nada obriga a venda <= compra na criação de uma loja do servidor: `C 5 / V 10` permite um ciclo de dinheiro infinito (só admins). Validar em `SignShopImpl.createShop`.
- Se o jogador sair entre o `withdraw` e a entrega na thread principal, o `give` vai para um `Player` offline e os itens perdem-se. Verificar `isOnline()` e reembolsar.
- Teste manual no jogo: clicar numa placa de loja antes do `/login` não faz nada; admins têm de agachar-se (sneak) para partir uma placa de loja, porque o clique esquerdo é cancelado.
- Menu da loja: shift + botão direito abre um baú com o item (tooltip mostra encantamentos), comprar à esquerda e vender à direita; o item mostrado é limitado ao tamanho da pilha.
- Teste manual no jogo: (1) shift + botão direito numa loja com item abre o menu; (2) o tooltip do item mostra os encantamentos; (3) clicar no item, num espaço vazio, com shift-click de itens do teu inventário, com teclas numéricas, a arrastar e com duplo clique não move nada; (4) comprar e vender pelos botões dá as mesmas mensagens e o novo saldo; (5) partir a placa com o menu aberto e clicar num botão fecha o menu e avisa; (6) shift + botão esquerdo continua a partir a placa (admin); (7) shift + botão direito numa loja sem item não abre menu; (8) sem `/login` não abre.
- Ideia futura: botões de quantidade (x1, x8, x64) no menu.
- Teste manual no jogo: abrir o menu, fazer /reload e confirmar que o menu fecha e não dá para tirar o item.
- O menu e a transação não verificam a distância à placa (um jogador pode abrir o menu, afastar-se e continuar a trocar; `getBlockAt` pode carregar o chunk da placa).
- Se o admin redefinir o item com um menu aberto, o menu continua a mostrar o item antigo (a troca já usa o novo).
