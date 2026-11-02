// 128782 - João Mota
// Classe criada DE PROPOSITO para provocar violacoes da regra MethodName (lowerCamelCase)
// e testar o quality gate Checkstyle (passo 4 da Tarefa 2B). NAO integrar no main.
// Versao CORRIGIDA: metodos renomeados para lowerCamelCase.
package battleship;

public class NamingDemo {

    // Era CalcularPontos (comecava por maiuscula)
    public int calcularPontos(int acertos) {
        return acertos * 10;
    }

    // Era calcular_bonus (usava underscore)
    public int calcularBonus(int afundados) {
        return afundados * 50;
    }
}
