<!-- 83590 - Valentyna Rusinova -->
# Quality gate Checkstyle – convenção de nomes das constantes (83590)

## Ficheiros

| Ficheiro | Localização |
|---|---|
| Workflow | `.github/workflows/checkstyle-83590.yaml` |
| Configuração do Checkstyle | `Checkstyle/checkstyle-83590.xml` |

## Convenção escolhida

Tipo de identificador: **constantes** (campos `static final`). Regra do Checkstyle: `ConstantName`, com o padrão `^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$` (MAIÚSCULAS separadas por sublinhado, por exemplo `MAX_SHIPS`). O ficheiro de configuração só tem esta regra, com `severity=error`.

Action usada: `dbelyaev/action-checkstyle@v4` (reporter `github-pr-check`, `level: error`, `fail_level: error`, `filter_mode: nofilter`). O workflow corre em *pull request* e falha quando existe alguma violação.

## Execuções (violação provocada e corrigida)

| Execução | Resultado | Ligação |
|---|---|---|
| Com a violação (PR) | Gate falha | _(colar a ligação da execução)_ |
| Depois de corrigir | Gate passa | _(colar a ligação da execução)_ |

Violação provocada: _(indicar o ficheiro, a linha e o nome, por exemplo `maxShips` em vez de `MAX_SHIPS`)_.

## Proteção do ramo `main`

_(Indicar se a regra foi configurada e se o PR com a violação ficou com o merge bloqueado. A conta `LEI-83590` não tem permissão de administrador no repositório, por isso a regra depende do dono, `LEI-128791`.)_

## O que o Checkstyle não consegue verificar

_(2 ou 3 frases, a preencher depois das execuções. Ideias: o Checkstyle só valida a forma do nome (o padrão), não se o nome é significativo ou claro (`MAX_SHIPS` e `X1` cumprem os dois a regra); não sabe se um `static final` devia mesmo ser constante, e ignora campos que não sejam `static final`, como constantes escritas em atributos de instância; e a regra só vê o código Java, não a documentação nem os testes dos nomes.)_
