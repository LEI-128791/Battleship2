# Prompts para jogar contra o Gemini

Envie as mensagens **pela ordem**, cada uma como uma mensagem separada no chat. Cada bloco de
texto é para copiar tal e qual.

---

## Mensagem 1 — Regras do jogo (enunciado, parte C.2)

```text
Considere que é um perito no famoso jogo da Batalha Naval, aqui numa versão do tempo
dos Descobrimentos Portugueses, jogado num tabuleiro com linhas identificadas de A a
J e colunas de 1 a 10. Deve começar por criar secretamente a sua frota de 11 navios:
• 4 Barcas (1 posição na quadrícula)
• 3 Caravelas (2 posições na quadrícula)
• 2 Naus (3 posições na quadrícula)
• 1 Fragata (4 posições na quadrícula)
• 1 Galeão (5 posições na quadrícula, em forma de T, com um corpo de 3 posições
e, numa das suas extremidades, uma posição adicional para cada lado, correspon-
dentes às chamadas "asas da ponte")
Os navios podem ser gerados com qualquer orientação no tabuleiro, desde que não
toquem uns nos outros, nem mesmo por um canto (i.e. na diagonal), embora possam
estar encostados às margens do tabuleiro. Repare que enquanto uma caravela, uma nau
ou uma fragata pode ter duas orientações possíveis (norte-sul ou este-oeste), um galeão
pode ter quatro orientações diferentes, consoante o braço do T esteja virado para norte,
sul, este ou oeste. Já a questão da orientação não se coloca para uma barca, porque
ocupa apenas uma posição no tabuleiro.
```

## Mensagens 2 a 5 — Confirmar as formas do Galeão (enunciado, parte C.3)

Envie uma de cada vez e confirme que o tabuleiro respeita as regras.

```text
Mostre-me um tabuleiro criado com estas regras, com o Galeão virado a Norte. Deixe
as posições da água em branco para melhor visualização das posições dos navios.
```
```text
Mostre-me um tabuleiro criado com estas regras, com o Galeão virado a Sul.
```
```text
Mostre-me um tabuleiro criado com estas regras, com o Galeão virado a Este.
```
```text
Mostre-me um tabuleiro criado com estas regras, com o Galeão virado a Oeste.
```

## Mensagem 6 — Protocolo de comunicação (enunciado, parte C.4)

```text
A nossa interação será através de objetos JSON, quer para a rajada de 3 tiros, quer para
a correspondente resposta. Uma rajada tem o seguinte formato:
{
[ {"row": "A", "column": 5},
{"row": "C", "column": 10},
{"row": "F", "column" : 5} ]
}
A resposta a uma rajada é feita em conjunto e não tiro a tiro, como nos seguintes 3
exemplos:
• Uma barca ao fundo, um tiro numa nau e um tiro na água
{
"validShots": 3,
"sunkBoats": [ {"count": 1, "type": "Barca"} ],
"repeatedShots": 0,
"outsideShots": 0,
"hitsOnBoats": [ {"hits": 1, "type": "Nau"} ],
"missedShots": 1
}
• Um tiro fora do tabuleiro, um tiro repetido e um tiro na água (o único válido)
{
"validShots": 1,
"sunkBoats": [ ],
"repeatedShots": 1,
"outsideShots": 1,
"hitsOnBoats": [ ],
"missedShots": 1
}
• Um tiro numa nau, um tiro no galeão e uma caravela ao fundo
{
"validShots": 3,
"sunkBoats": [{"count": 1, "type": "Caravela"}],
"repeatedShots": 0,
"outsideShots": 0,
"hitsOnBoats": [{"hits": 1, "type": "Nau"}, {"hits": 1, "type": "Galeao"}],
"missedShots": 0
}
O campo "repeatedShots" indica o número de tiros em posições já atingidas em jogadas
anteriores, ou na própria jogada (em ambos os casos, uma péssima estratégia porque
desperdiça tiros, só admissível por distração do jogador). O campo "outsideShots" indica
o número de tiros em posições exteriores ao tabuleiro (ex.: B12, K4 ou L14), um erro
também só admissível por distração do jogador. O campo "missedShots" corresponde ao
número de tiros na água.
Sugiro que me mande agora várias rajadas de tiros (uma de cada vez) e eu devolvo-lhe
o JSON da resposta. Pare só quando tiver aprendido o protocolo de comunicação do
jogo.
```

Depois desta mensagem, o Gemini começa a enviar rajadas. Responda-lhe com o JSON que o
programa imprime (ver "Como jogar", abaixo) até ele dizer que aprendeu o protocolo.

## Mensagem 7 — Estratégia (versão 2)

O porquê de cada regra e as mensagens de correção para usar durante o jogo estão em
[PROMPT_ESTRATEGIA.md](PROMPT_ESTRATEGIA.md).

```text
Considere agora a seguinte tática de geração de rajadas de tiros. Objetivo: afundar os 11 navios
inimigos (25 casas) no MENOR número de rajadas. Cada rajada tem SEMPRE 3 tiros.

========== MEMÓRIA: O DIÁRIO DE BORDO ==========
Cada resposta minha traz o JSON, o "Diário de Bordo" (todas as rajadas, com os 3 tiros e o resultado)
e os "Tiros já disparados". O Diário é a verdade: em cada jogada, reconstrua tudo a partir dele e
não da sua memória. A resposta diz o resultado da rajada em conjunto, NÃO diz qual tiro acertou.

========== NAVIOS ==========
Barca 1 · Caravela 2 (x3) · Nau 3 (x2) · Fragata 4 · Galeão 5 (x1). Barcas x4.
Caravela, Nau e Fragata são linhas retas (horizontal ou vertical). O Galeão é um T:
  Norte    Sul      Este     Oeste
  # # #    . # .    . . #    # . .
  . # .    . # .    # # #    # # #
  . # .    # # #    . . #    # . .
Os navios NUNCA se tocam, nem na diagonal: as 8 casas à volta de um navio são água.

========== FORMATO DE CADA RESPOSTA (sempre estas 4 partes) ==========
1) FALTAM: navios ainda a flutuar.
2) AFUNDADOS: cada navio afundado com as casas CERTAS dele (ex.: "Fragata F2 F3 F4 F5").
   Se não sabe as casas: "Barca (A3 ou A7 ou G5)".
3) ALVOS ABERTOS: cada navio atingido e ainda a flutuar, por exemplo:
   "Nau — certo: H8; mais 1 casa em H7, H9 ou G8"  ou  "Nau — 1 acerto em H8, E3 ou C9 (TESTE)".
   Se não houver nenhum, escreva "nenhum".
4) RAJADA: os 3 tiros, cada um com UMA razão curta ("H9 — caça Nau, Este de H8"). Depois:
   "Verificação: nenhum tiro está nos Tiros já disparados nem encostado a uma casa certa de AFUNDADOS."
   Por fim, estas 2 linhas com os mesmos 3 tiros (a 2.ª é o comando do jogo):
   { [ {"row": "H", "column": 9}, {"row": "E", "column": 4}, {"row": "C", "column": 8} ] }
   rajada H9 E4 C8

========== ORDEM DE DECISÃO (por esta ordem, sem exceções) ==========
1. CAÇA — há um ALVO com pelo menos uma casa CERTA? Os 3 tiros vão para ele.
2. TESTE — há um ALVO só com candidatos (acerto num de vários tiros)? Os 3 tiros testam um candidato.
3. PROCURA — só se não houver nenhum ALVO ABERTO.
Se a CAÇA ou o TESTE não dão 3 tiros úteis, complete com PROCURA longe do alvo (3 ou mais casas).
Nunca abandone um ALVO ABERTO para ir procurar: um acerto esquecido custa muitas rajadas.

========== CAÇA ==========
C1. Uma casa certa X: dispare nos vizinhos de X (Norte, Sul, Este, Oeste) que ainda estão livres.
    Nunca nas diagonais de X: são água (só o Galeão, por ser um T, foge a esta regra).
C2. Duas ou mais casas certas em linha: dispare só nessa linha, na casa seguinte de cada ponta
    (e na seguinte a essa, se sobrar um tiro). Nunca ao lado da linha.
C3. Um acerto num de vários vizinhos (ex.: "1 tiro na Nau" depois de H7 H9 G8 à volta de H8):
    o navio passa por X e por um deles. Dispare na casa seguinte em linha de cada hipótese
    (H6 para H7, H10 para H9, F8 para G8). Não precisa de saber qual foi: o "ao fundo" chega sozinho.
C4. Use o tamanho: um navio nunca tem mais acertos do que casas. Uma Nau com 2 acertos precisa de
    mais 1; quando a resposta disser "Nau ao fundo", pare de disparar nesse alvo.
C5. Galeão: se as casas certas não estão em linha, ou a linha já tem 3 e o navio não afundou,
    complete uma das 4 formas do T desenhadas acima.

========== TESTE (acerto ambíguo numa rajada de PROCURA) ==========
T1. Os candidatos são os tiros dessa rajada que não são água certa. Risque os que não podem ser esse
    navio: o tipo não cabe lá (falta espaço livre em linha para o tamanho dele) ou estão encostados
    a uma casa certa de um AFUNDADO.
T2. Teste o candidato com mais vizinhos livres: dispare 3 dos vizinhos dele (Norte, Sul, Este, Oeste).
T3. A resposta tem acerto nesse tipo de navio? Então o candidato era o navio: passa a CAÇA com
    "certo: <candidato>; mais 1 casa em <os 3 vizinhos>" (regra C3). Os outros candidatos dessa
    rajada de PROCURA eram água, se ela só teve 1 acerto.
    Não tem acerto? Esse candidato era (quase de certeza) água: risque-o e teste o seguinte.
    Se só sobrar 1 candidato, é certo: passa a CAÇA.

========== AFUNDADOS ==========
A1. "X ao fundo" e havia um ALVO ABERTO desse tipo: o navio afundado é esse alvo. Escreva em
    AFUNDADOS as casas que são certas. A partir daí nunca dispare nas 8 casas à volta delas.
A2. "Barca ao fundo" numa rajada de PROCURA: não sabe qual dos tiros foi. Escreva
    "Barca (A3 ou A7 ou G5)" e não dispare à volta. A Barca está acabada: não precisa de mais tiros.
A3. Uma rajada pode ter, ao mesmo tempo, um navio ao fundo e um acerto noutro navio. Trate cada um
    pela sua regra.

========== PROCURA ==========
Percorra a lista por ordem e use as primeiras casas que estejam livres. Salte casas já disparadas,
encostadas (mesmo na diagonal) a uma casa certa de navio, ou que sejam candidatos de um TESTE.
   Fase 1: H8 E3 C9 | I3 B10 F10 | C5 J2 I7 | A3 A7 G5 | B6 F2 G9 | D4 D8 H4 | J6 B2 F6 | E7 G1 C1 | J10
   Fase 2: I9 G3 A5 | H2 D10 B4 | E9 I1 A9 | C7 H6 H10 | D6 D2 I5 | E1 E5 A1 | G7 B8 C3 | F4 F8 J4 | J8
   Fase 3 (lista acabada ou só faltam Barcas): escolha casas livres que não toquem em nenhum navio,
   começando pelas zonas maiores sem tiros.
ÚLTIMA RAJADA: se faltarem menos de 3 casas onde ainda pode haver navio, complete os 3 tiros com
casas já disparadas (é a única exceção a "nunca repetir").

========== EXEMPLOS ==========
--- Exemplo A: acerto ambíguo na procura -> TESTE ---
Recebe: Diário de Bordo: [R1 H8 E3 C9 = 3 tiros válidos: 1 tiro num(a) Nau + 2 tiros na água]
Responde:
1) FALTAM: Galeão, Fragata, Nau x2, Caravela x3, Barca x4
2) AFUNDADOS: nenhum
3) ALVOS ABERTOS: Nau — 1 acerto em H8, E3 ou C9 (TESTE). Os 3 têm 4 vizinhos livres; testo H8.
4) RAJADA:
   H7 — teste de H8, Oeste
   H9 — teste de H8, Este
   G8 — teste de H8, Norte
   Verificação: nenhum tiro está nos Tiros já disparados nem encostado a uma casa certa de AFUNDADOS.
   { [ {"row": "H", "column": 7}, {"row": "H", "column": 9}, {"row": "G", "column": 8} ] }
   rajada H7 H9 G8

--- Exemplo B: o teste acerta -> CAÇA ---
Recebe: Diário de Bordo: [R1 H8 E3 C9 = 3 tiros válidos: 1 tiro num(a) Nau + 2 tiros na água;
R2 H7 H9 G8 = 3 tiros válidos: 1 tiro num(a) Nau + 2 tiros na água]
Responde:
1) FALTAM: Galeão, Fragata, Nau x2, Caravela x3, Barca x4
2) AFUNDADOS: nenhum
3) ALVOS ABERTOS: Nau — certo: H8; mais 1 casa em H7, H9 ou G8. (E3 e C9 eram água: a R1 só teve
   1 acerto.) Hipóteses da Nau (3 casas): H6 H7 H8, H8 H9 H10, F8 G8 H8 ou G8 H8 I8.
4) RAJADA:
   H6 — caça Nau, a seguir a H7
   H10 — caça Nau, a seguir a H9
   F8 — caça Nau, a seguir a G8
   Verificação: nenhum tiro está nos Tiros já disparados nem encostado a uma casa certa de AFUNDADOS.
   { [ {"row": "H", "column": 6}, {"row": "H", "column": 10}, {"row": "F", "column": 8} ] }
   rajada H6 H10 F8

--- Exemplo C: só 1 casa útil para a caça; o navio vai ao fundo -> AFUNDADOS e PROCURA ---
Antes: ALVOS ABERTOS: Fragata — certo: C5 C6 C7 (C4 já foi água). Só sobra uma ponta: C8.
Por isso a rajada foi C8 (caça) + I3 e B10 (procura, longe da Fragata).
Recebe: Diário de Bordo: [...; R6 C8 I3 B10 = 3 tiros válidos: 1 Fragata ao fundo + 2 tiros na água]
Responde:
1) FALTAM: Galeão, Nau x2, Caravela x3, Barca x4
2) AFUNDADOS: Fragata C5 C6 C7 C8 (a Fragata tem 4 casas; a única que faltava era C8, por isso
   I3 e B10 foram água)
3) ALVOS ABERTOS: nenhum
4) RAJADA: PROCURA. Salto C5 (é a Fragata) e qualquer casa encostada a C5 C6 C7 C8.
   F10 — procura, Fase 1
   J2 — procura, Fase 1
   I7 — procura, Fase 1
   Verificação: nenhum tiro está nos Tiros já disparados nem encostado a uma casa certa de AFUNDADOS.
   { [ {"row": "F", "column": 10}, {"row": "J", "column": 2}, {"row": "I", "column": 7} ] }
   rajada F10 J2 I7

--- Exemplo D: fim do jogo, só falta 1 Barca ---
Responde:
1) FALTAM: Barca x1
2) AFUNDADOS: (todos os outros, com as casas certas)
3) ALVOS ABERTOS: nenhum
4) RAJADA: PROCURA, Fase 3. Casas livres que não tocam em nenhum navio: B9, E1, J5 e J6.
   Escolho as 3 que estão mais longe de tudo.
   B9 — procura, só cabe Barca
   E1 — procura, só cabe Barca
   J5 — procura, só cabe Barca
   Verificação: nenhum tiro está nos Tiros já disparados nem encostado a uma casa certa de AFUNDADOS.
   { [ {"row": "B", "column": 9}, {"row": "E", "column": 1}, {"row": "J", "column": 5} ] }
   rajada B9 E1 J5

Se a sua frota for toda afundada, declare a derrota com honra. Se o inimigo se render com todos os
navios no fundo do oceano, seja um vencedor magnânimo!
```

## (Opcional) Mensagem 8 — Só se também disparar contra a frota do Gemini

O prompt de estratégia foi testado com o LLM só a atacar. Se, no vosso jogo, também
disparar contra a frota do Gemini, envie esta mensagem a seguir à estratégia.
**Ainda não foi testada.**

```text
Se eu também lhe enviar uma rajada minha, comece a resposta pelo JSON de resposta a essa
rajada (no formato do protocolo) e só depois escreva as 3 partes.
```

---

## Como jogar

1. Corra o jogo, escolha `pt` e escreva `gerafrota` (ou `lefrota` para uma frota fixa).
2. Quando o Gemini enviar uma rajada, copie a linha `rajada H7 E4 C8` que ele escreve a seguir ao JSON
   e cole-a no jogo. Em alternativa, escreva `rajada` e cole o JSON (numa linha, em várias ou num bloco ```json).
3. O programa mostra o resultado e imprime uma linha a seguir a
   **"Resposta para colar no chat do LLM:"**. **Cole essa linha inteira no chat.** Leva o JSON
   da resposta, o Diário de Bordo (todas as rajadas e o resultado de cada uma), a lista de todos
   os tiros que o Gemini já disparou e um lembrete do formato.
   Assim não precisa de lhe dizer mais nada.
4. Repita até o programa mostrar `Estado da Frota: 0 a flutuar`.

Resultados: com a versão 1 do prompt, o ChatGPT e o Gemini precisaram de 31 a 34 rajadas. Num
simulador que segue as regras da versão 2 à letra, a média é 22,8 rajadas (80% dos jogos entre
20 e 25). Se o LLM se enganar, use as mensagens de correção do
[PROMPT_ESTRATEGIA.md](PROMPT_ESTRATEGIA.md).
