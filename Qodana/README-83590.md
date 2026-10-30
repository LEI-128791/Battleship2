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

Pull request: https://github.com/LEI-128791/Battleship2/pull/12 (ramo `teste-long-method-83590`, fechado sem merge).

Método provocado: `LongMethodDemo.longMethod()`, em `Battleship2-main/src/main/java/battleship/LongMethodDemo.java`, com **81 instruções** (limiar 75). A inspeção indica: `'longMethod' is too long (# Non-comment source statements = 81)`. Correção: o método foi substituído por `shortMethod()`, com 3 instruções.

## Proteção do ramo `main` (passo 10)

Não foi possível configurar a regra de proteção: a conta `LEI-83590` tem permissão de escrita (`push`), mas não de administrador, e as definições do repositório (*Settings > Branches*) só estão disponíveis para o dono, `LEI-128791`. O pedido foi feito ao dono do repositório. Por isso, para já, o botão de merge do PR #12 **não ficou bloqueado** com o gate vermelho. Esta secção será atualizada quando a regra for ativada (exigir o check `qodana`).

## Reflexão

A inspeção `NonCommentSourceStatements` detetou o método artificial de 81 instruções logo à primeira (ficheiro e linha corretos), e no código real só dispara quando se baixa o limiar: com `m_limit` 75 não há nenhum método (o maior, `Move.processEnemyFire`, tem 71 instruções), com 30 há 13 e com 20 há 21 (por exemplo `Tasks.menu`, `BattleshipGUI.handleTurn` e `BoardView.set`), por isso o resultado é muito sensível ao limiar e o 75 foi escolhido para o gate passar no código atual. Conta instruções e não linhas ou complexidade: um método com muitas instruções simples (como o construtor de `BoardView`) é assinalado, e um com pouca extensão mas muitos ramos não seria (`Move.processEnemyFire` tem CC 37), o que dá falsos positivos e falsos negativos face ao Long Method. Face ao livro (*Object-Oriented Metrics in Practice*), o Qodana usa um único limiar absoluto e não permite combinar LOC e CC com um AND, nem limiares relativos ao sistema, pelo que é só uma aproximação. Na configuração, o formato `profile.path` da ficha e o `profile.base.path` não aplicaram o perfil (o Qodana usava o perfil por omissão, com 244 problemas), e só o `imports` no `qodana.yaml` funcionou.
