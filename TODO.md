# TODO — fShop

Pendências conhecidas deste plugin (15/10/2026). Riscar quando feito.

## Funcionalidades
- [x] Ligar as lojas à economia (fEconomy) via `EconomyApi` do fAPI.
- [x] Guardar o item de cada loja do servidor (tabela fshop_items, cache em memória).
- [x] Proteção das lojas: cada jogador só parte a sua loja de jogador, os admins (`fshop.admin`) partem tudo e as lojas do servidor só os admins (`ShopProtectListener`).

## Código
- [ ] O `MessagesManager` lê as mensagens de `getConfig()` (`Mensagens.playeroff`, etc.), mas o plugin não tem `config.yml` nos recursos nem chama `saveDefaultConfig()`: os valores vêm `null`. Criar o `config.yml` e gravá-lo por omissão.
- [ ] `SignShopManager`: criar uma exceção própria (comentário `TODO` no código).
- [ ] `SignShopManager`: mover a lógica marcada para o fAPI (comentário `TODO` no código).

## Qualidade
- [x] Testes automáticos (`ShopOffer`, `ShopTrade`, `SignShopManager`).

## Depois da integração com a economia
- [x] Lojas de jogador: ver a secção "Lojas de jogador (baú)" abaixo.
- Código morto a decidir: `MessagesManager`, `VariablesManager`, `ItemAddCommand` (referem o plugin antigo).
- `removeColorCodes` do `SignShopManager` devia ir para o fAPI.
- Dependência `sqlite-jdbc` do `build.gradle.kts` já não é usada.
- Se o preço da placa for demasiado alto (ex.: 90 mil milhões), o `deposit` pode estourar o `BIGINT` e a venda dá erro genérico.
- Placas apagadas por outros meios que não partir (física, explosões, `/setblock`) deixam uma linha órfã em `fshop_items` (inofensiva; é limpa quando se recria uma placa no mesmo sítio).
- Linhas de `fshop_items` que não descodificam são ignoradas em silêncio no arranque (registar).
- Um item definido gasto/danificado é entregue gasto (a venda ignora durabilidade, a compra não a repõe).
- Os itens das lojas de jogador (baú) também usam `fshop_items`.
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
- Teste manual no jogo: (1) shift + botão direito numa loja com item abre o menu; (2) o tooltip do item mostra os encantamentos; (3) clicar no item, num espaço vazio, com shift-click de itens do teu inventário, com teclas numéricas, a arrastar e com duplo clique não move nada; (4) comprar e vender pelos botões dá as mesmas mensagens e o novo saldo; (5) partir a placa com o menu aberto e clicar num botão fecha o menu e avisa; (6) shift + botão esquerdo continua a partir a placa (admin); (7) shift + botão direito numa loja sem item não abre menu; (8) sem `/login` não abre; (9) o menu continua igual depois de passar a usar `FInventory`/`FItemStack` do fAPI: item ao centro, lã verde à esquerda e vermelha à direita, botões com nome e preço na lore.
- Ideia futura: botões de quantidade (x1, x8, x64) no menu.
- Teste manual no jogo: abrir o menu, fazer /reload e confirmar que o menu fecha e não dá para tirar o item.
- O menu e a transação não verificam a distância à placa (um jogador pode abrir o menu, afastar-se e continuar a trocar; `getBlockAt` pode carregar o chunk da placa).
- Se o admin redefinir o item com um menu aberto, o menu continua a mostrar o item antigo (a troca já usa o novo).
- Proteção das lojas: só cobre `BlockBreakEvent`. Explosões, o bloco de suporte da placa partido (a placa cai sozinha), pistões e fogo ainda podem destruir lojas. Uma placa qualquer cuja linha 3 pareça `C 10` conta como loja e fica protegida (`SignShopManager.isShop`).
- Teste manual no jogo: sem `fshop.admin`, agachado e botão esquerdo numa loja do servidor ou de outro jogador não parte e mostra "Não podes partir esta loja."; partir a própria loja de jogador funciona; um admin parte tudo.
- Risco residual nas lojas de jogador: se o dono esvaziar o baú (por exemplo com um funil) enquanto o pagamento de uma venda está em curso e o pagamento falhar por outro motivo que não saldo (ex.: o dono gastou o dinheiro entretanto), os itens do vendedor são devolvidos e ficam duplicados; mitigado pela verificação prévia de saldo; a solução completa seria bloquear o baú durante a troca.

## Lojas de jogador (baú)
- [x] Lojas de jogador: placa de parede presa a um baú; troca pelo baú real; dono por UUID em `fshop_shops`; aviso ao dono por chat se estiver online (offline não recebe nada).
- Limites: sem proteção de baús (fica no plugin de terrenos); a compra a partir do baú entrega o item do modelo (durabilidade do modelo), não a pilha exata do baú; baú partido deixa a linha de `fshop_shops` órfã até se recriar a placa; sem limite nem custo de lojas por jogador; placas de jogador antigas têm de ser recriadas; ideia futura: notificar donos offline, registo de transações.
- Teste manual no jogo: (1) placa de chão ou de parede sem baú: a placa parte-se com "tem de estar presa a um baú"; (2) criar com baú simples e com baú duplo; (3) o dono define o item com o item na mão; outro jogador não consegue; (4) comprar com stock, sem stock, com o dono a ser o próprio (recusa); (5) vender com o baú cheio e com o dono sem dinheiro; (6) o dono online recebe a mensagem de compra e de venda; offline não; (7) shift + botão direito abre o menu "Loja de <nome>"; (8) um baú colado à placa mas não preso a ela (placa noutra face) não conta; (9) reiniciar o servidor mantém o dono; (10) partir a placa apaga o registo e a loja recriada no mesmo sítio não herda o dono; (11) só o dono e admins partem a placa; (12) uma placa de jogador antiga diz que é antiga.
