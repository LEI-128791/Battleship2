# Prompt de estratégia (Sprint parte 2) — versão 2

Este é o prompt que se envia **depois** das mensagens 1 a 6 do [PROMPTS_GEMINI.md](PROMPTS_GEMINI.md)
(regras, formas do Galeão e protocolo JSON). Copie o bloco abaixo tal e qual, numa só mensagem.

Objetivo: ganhar em **23 a 25 rajadas**. Num simulador que segue estas regras à letra
(400 frotas aleatórias), a média é 22,8 rajadas e 80% dos jogos ficam entre 20 e 25.

## O que mudou em relação à versão 1 (e porquê)

Nos jogos gravados (`chatgpt1`, `chatgpt2`, `gemini1`, `gemini2`) o LLM precisou de 31 a 34 rajadas,
ou seja, quase o tabuleiro inteiro. Os problemas eram estes:

| Problema nos jogos | Correção |
|---|---|
| Esquecia os resultados das rajadas antigas e trocava acertos por água | O programa passa a colar o **Diário de Bordo** completo (cada rajada e o seu resultado) em cada resposta. O LLM reconstrói tudo a partir dele, sem depender da memória. |
| O mapa 10x10 desenhado em texto estragava-se (no Gemini saía numa só linha) | Já não há mapa. O LLM escreve só 4 listas curtas: FALTAM, AFUNDADOS, ALVOS ABERTOS e RAJADA. |
| Acertava num navio e voltava à procura (ex.: `chatgpt2` varreu as linhas I e J coluna a coluna) | Uma **ordem de decisão** fixa: primeiro CAÇA, depois TESTE e só no fim PROCURA. |
| "1 tiro numa Nau" em 3 tiros espalhados ficava como "?" e nunca se resolvia | O **TESTE**: um candidato de cada vez, com 3 tiros à volta dele. A resposta seguinte diz se era esse. |
| A VERIFICAÇÃO "SIM SIM SIM" era preenchida sem verificar nada | Cada tiro leva **uma razão** ("vizinho Este de H8") e há uma só verificação, a que interessa. |
| Só havia 1 exemplo | **4 exemplos** few-shot completos, com os casos difíceis. |

---

## Mensagem 7 — Estratégia

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

---

## Mensagens de correção (few-shot durante o jogo)

Quando o LLM se engana, cole uma destas mensagens **antes** da resposta do programa, na mesma
mensagem. Troque os valores entre `<>`.

```text
Correção: esqueceu o ALVO ABERTO <Nau, certo: H8>. Pela ORDEM DE DECISÃO, os 3 tiros têm de ir para ele.
Refaça a rajada.
```
```text
Correção: <C9> está encostada à <Fragata C5 C6 C7 C8>, que já afundou. É água certa. Troque esse tiro.
```
```text
Correção: <H8> já está nos Tiros já disparados. Troque esse tiro.
```
```text
Correção: a rajada <R3> só teve 1 acerto e o TESTE confirmou <H8>. Então <E3> e <C9> eram água:
tire-os dos candidatos.
```

## Dicas para o jogo correr bem

- Use o modo com raciocínio (Gemini 2.5 Pro / ChatGPT com "Pensar"). Os modelos rápidos perdem-se mais.
- Uma conversa nova por jogo. Uma conversa muito comprida faz o modelo esquecer o prompt.
- Para comparar versões do prompt, jogue com a mesma frota (`lefrota`).
- Se começar a varrer linhas inteiras ou a ignorar alvos, cole a primeira mensagem de correção.
