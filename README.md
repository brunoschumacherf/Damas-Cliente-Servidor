# Jogo de Damas - Cliente/Servidor

## Integrantes
- Bruno Schumacher Farias Souza
- Erick Batista
- Henio Pedro Silva Santana
- Thiago Souza Oliveira

## Descrição
Jogo de damas multiplayer onde dois clientes se conectam a um servidor central para jogar em tempo real.

## Requisitos
- Java 8 ou superior
- Dois computadores na mesma rede

## Como Executar
### Servidor
```bash
javac jogo/*.java
java jogo.DamasServer

# Protocolo de Aplicação - Jogo de Damas

## Mensagens
| Mensagem | Direção | Descrição |
|----------|---------|-----------|
| `INICIO <cor>` | Servidor → Cliente | Informa a cor do jogador (BRANCAS/PRETAS) |
| `TABULEIRO <estado>` | Servidor → Cliente | Envia o estado atual do tabuleiro serializado |
| `TURNO <cor>` | Servidor → Cliente | Indica de quem é a vez (BRANCAS/PRETAS) |
| `MOVIMENTO <origem> <destino>` | Cliente → Servidor | Envia jogada (ex: `3A 4B`) |
| `ERRO <mensagem>` | Servidor → Cliente | Notifica erro no movimento |
| `VITORIA <cor>` | Servidor → Cliente | Anuncia o vencedor |
| `HIST` | Cliente → Servidor | Solicita histórico de jogadas |
| `HIST <jogada>` | Servidor → Cliente | Retorna uma jogada do histórico |
| `SAIR` | Cliente → Servidor | Encerra a conexão |

## Fluxo do Protocolo
1. **Conexão**: Cliente conecta ao servidor na porta 5555
2. **Aguardando**: Servidor espera 2 clientes
3. **Início**: Servidor envia `INICIO BRANCAS` e `INICIO PRETAS`
4. **Turno**: Servidor envia `TURNO BRANCAS` ou `TURNO PRETAS`
5. **Jogada**: Cliente envia `MOVIMENTO origem destino`
6. **Atualização**: Servidor valida e envia `TABULEIRO` para ambos
7. **Fim**: Servidor envia `VITORIA` quando alguém ganha

## Transporte
- **Protocolo**: TCP (Socket)
- **Porta**: 5555
- **Motivo**: Garante entrega confiável e ordenada das mensagens

## Formato do Tabuleiro
Serializado como: `vezBrancas;PEAO_BRANCO,VAZIO,DAMA_PRETA,...`
- Primeiro caractere: `1` = vez das brancas, `0` = vez das pretas
- Sequência de 64 peças separadas por vírgula
