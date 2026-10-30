<!-- 83590 - Valentyna Rusinova -->
# Quality gate Qodana – cheiro "Long Method" (83590)

## Ficheiros

| Ficheiro | Localização |
|---|---|
| Workflow | `.github/workflows/qodana-smell-83590.yaml` |
| Configuração do linter | `Battleship2-main/qodana-83590.yaml` |
| Perfil de inspeção | `Battleship2-main/.qodana/profiles/profile-83590.yaml` |

Linter: `jetbrains/qodana-jvm-community:2026.2` · JDK do projeto: 21 · Action: `JetBrains/qodana-action@v2026.2`.

**Como o perfil é ligado:** o `qodana-83590.yaml` usa `profile.base.name: empty` (nenhuma inspeção ligada) e `imports` para importar o perfil `.qodana/profiles/profile-83590.yaml`, que liga só a inspeção do Long Method. Na versão 2026.2 do linter, o formato da Listing 1 da ficha (`profile.path`) e o `profile.base.path` não aplicaram o perfil (o Qodana usava o perfil `Default`, com 244 problemas). O `--profile-path` também não. Só o `imports` funcionou, confirmado a correr o Qodana localmente.

## Tabela de mapeamento (passo 4)

| Cheiro (Fowler) | Inspeção no IDE | ID da inspeção | Métrica / limiar | Limitações |
|---|---|---|---|---|
| Long Method | Overly long method (`Settings > Editor > Inspections > Java > Method metrics`) | `NonCommentSourceStatements` (a página do Inspectopedia chama-lhe também `OverlyLongMethod`) | Instruções (*statements*) por método. Opção `m_limit`: por omissão 30; usado 75 | Não permite combinar LOC e CC com um AND, como o livro. Limiar absoluto, não relativo ao sistema. Conta instruções e não linhas (LOC). Não conta instruções vazias, blocos nem a inicialização e o incremento do `for` |

**Valor do limiar:** o livro (*Object-Oriented Metrics in Practice*, p. 16, tabela 2.2) dá, para Java, LOC/Method de 7 (baixo), 10 (médio), 13 (alto) e 19,5 (muito alto). Comecei com 20 instruções, próximo do limiar "muito alto", mas o projeto tem 21 métodos acima disso (13 acima de 30), e o gate falhava logo no código atual. Subi o limiar para 75, porque o maior método (`Move.processEnemyFire`) tem 71 instruções, de modo que o gate passa no código atual e falha quando se introduz um método maior. A inspeção mede instruções e a tabela mede linhas, por isso é só uma aproximação.

**Disponibilidade:** a página do Inspectopedia indica que a inspeção existe no Qodana for JVM 2026.2, mas não menciona o linter Community. Confirmado: a inspeção `NonCommentSourceStatements` corre no `qodana-jvm-community:2026.2` (executada no GitHub Actions e localmente).

## Execuções do passo 9 (provocar o cheiro)

| Execução | Resultado | Ligação |
|---|---|---|
| Com o cheiro introduzido (PR #12) | Gate **falha**: 1 problema, `Overly long method` | https://github.com/LEI-128791/Battleship2/actions/runs/41130258808 |
| Depois de corrigir (PR #12) | Gate **passa**: 0 problemas | https://github.com/LEI-128791/Battleship2/actions/runs/41132734227 |

Pull request: https://github.com/LEI-128791/Battleship2/pull/12 (ramo `teste-long-method-83590`, fechado sem merge). O gate foi integrado no `main` pelo PR #11 (https://github.com/LEI-128791/Battleship2/pull/11) e corre em todos os pull requests (ficou verde nos PRs #13 e #14, que não tinham métodos longos).

Método provocado: `LongMethodDemo.longMethod()`, em `Battleship2-main/src/main/java/battleship/LongMethodDemo.java`, com **81 instruções** (limiar 75). A anotação do check no GitHub indica `LongMethodDemo.java:10`: `'longMethod' is too long (# Non-comment source statements = 81)`. Correção: o método foi substituído por `shortMethod()`, com 3 instruções.

## Proteção do ramo `main` (passo 10)

Não foi possível configurar a regra de proteção: a conta `LEI-83590` tem permissão de escrita (`push`), mas não de administrador, e as definições do repositório (*Settings > Branches*) só estão disponíveis para o dono, `LEI-128791`. O pedido foi feito ao dono do repositório. Por isso, para já, o botão de merge do PR #12 **não ficou bloqueado** com o gate vermelho. Esta secção será atualizada quando a regra for ativada (exigir o check `qodana`).

## Reflexão

A inspeção `NonCommentSourceStatements` detetou o método artificial de 81 instruções (anotação em `LongMethodDemo.java:10`), mas no código real o resultado depende muito do limiar: com `m_limit` 75 não há nenhum método (o maior, `Move.processEnemyFire`, tem 71 instruções), com 30 há 13 e com 20 há 21 (por exemplo `Tasks.menu`, `BattleshipGUI.handleTurn` e `BoardView.set`), por isso escolhi 75 para o gate passar no código atual. Como conta instruções e não linhas nem complexidade, assinala métodos longos mas simples (como o construtor de `BoardView`, com 37 instruções) e pode deixar passar um método curto com muitos ramos (`Move.processEnemyFire` tem CC 37), o que dá falsos positivos e falsos negativos face ao Long Method. Face ao livro (*Object-Oriented Metrics in Practice*), usa um só limiar absoluto, não combina LOC e CC com um AND e não usa limiares relativos ao sistema, pelo que é só uma aproximação. Na configuração, o `profile.path` da ficha e o `profile.base.path` não aplicaram o perfil (o Qodana usava o perfil `Default`, com 244 problemas) e só o `imports` no `qodana.yaml` funcionou.
