// 128782 - João Mota
// Classe criada DE PROPOSITO para provocar violacoes da regra MethodName (lowerCamelCase)
// e testar o quality gate Checkstyle (passo 4 da Tarefa 2B). NAO integrar no main.
package battleship;

public class NamingDemo {

    // Violacao 1: comeca por maiuscula (estilo de nome de classe)
    public int CalcularPontos(int acertos) {
        return acertos * 10;
    }

    // Violacao 2: usa underscore (estilo snake_case)
    public int calcular_bonus(int afundados) {
        return afundados * 50;
    }
}
