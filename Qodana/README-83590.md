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

**Disponibilidade:** a página do Inspectopedia indica que a inspeção existe no Qodana for JVM 2026.2, mas não menciona o linter Community. <!-- confirmar na primeira execução -->

## Execuções do passo 9 (provocar o cheiro)

| Execução | Resultado | Ligação |
|---|---|---|
| Com o cheiro introduzido (PR) | Gate falha | _(colar a ligação da execução)_ |
| Depois de corrigir | Gate passa | _(colar a ligação da execução)_ |

Método provocado: _(indicar o método e o número de instruções)_.

## Proteção do ramo `main` (passo 10)

_(Indicar se a regra de proteção foi configurada e se o botão de merge ficou bloqueado com o gate vermelho. Se o plano do repositório não o permitir, dizê-lo aqui.)_

## Reflexão

_(3 ou 4 frases, a preencher depois das execuções:)_
- _O que a ferramenta detetou (que métodos, em que ficheiros)._
- _Se houve falsos positivos ou falsos negativos._
- _Como o resultado muda com o limiar `m_limit` (por exemplo 30 contra 20)._
- _Limitações face à estratégia do livro (combinação de LOC e CC, limiares relativos ao sistema)._
