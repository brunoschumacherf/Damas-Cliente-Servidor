# Jogo de Damas - Cliente/Servidor

## Integrantes

- Bruno Schumacher Farias Souza
- Erick Batista
- Henio Pedro Silva Santana
- Thiago Souza Oliveira

## Descrição

Jogo de damas multiplayer em que dois clientes se conectam a um servidor central para jogar em tempo real. O servidor valida os movimentos, mantém o estado do tabuleiro e repassa as atualizações para os dois jogadores.

## Requisitos

- **Java 8 ou superior** instalado na máquina ([download](https://www.oracle.com/java/technologies/downloads/))
- **Dois computadores na mesma rede** (Wi‑Fi ou cabo)
- Um dos computadores fará de **servidor**; o outro (e o próprio servidor, para o segundo jogador) rodará o **cliente**

> Se alguém do grupo tiver notebook e puder instalar o Java, isso facilita os testes em sala.

---

## Tutorial: como abrir e testar o jogo

### 1. Baixar e preparar o projeto

1. Baixe o repositório (ZIP ou `git clone`) e extraia a pasta.
2. Abra o **Prompt de Comando** (cmd) ou **PowerShell**.

### 2. Descobrir o IPv4 do servidor

No computador que vai rodar o **servidor**, descubra o endereço IPv4 da rede local:

```cmd
ipconfig
```

Procure por **Endereço IPv4** na interface em uso (Wi‑Fi ou Ethernet). Exemplo: `192.168.1.105`.

> Na UESC o IPv4 será outro — repita esse passo sempre que mudar de rede.

### 3. Configurar o endereço no cliente

Abra o arquivo `src/jogo/DamasClient.java` e altere a variável `SERVER_ADDRESS` para o IPv4 do computador servidor:

```java
private static final String SERVER_ADDRESS = "192.168.1.105"; // seu IPv4 aqui
```

Salve o arquivo. **Todos os clientes** (inclusive o que roda no mesmo PC do servidor) precisam apontar para esse IP.

### 4. Compilar o projeto

No terminal, entre na pasta `src` do projeto:

```cmd
cd caminho\para\Damas-Cliente-Servidor\src
javac jogo/*.java
```

Se não aparecer erro, a compilação foi concluída.

### 5. Iniciar o servidor

Ainda dentro de `src`, execute:

```cmd
java jogo.DamasServer
```

Você deve ver algo como:

```
Servidor de Damas iniciado na porta 5555
```

O Windows pode exibir um aviso do **firewall** pedindo permissão para usar a **porta 5555** — aceite, pois é o programa do jogo.

O servidor fica aguardando **dois clientes** se conectarem.

### 6. Conectar os clientes

Abra **dois terminais** (podem ser no mesmo PC ou em máquinas diferentes na mesma rede). Em cada um:

```cmd
cd caminho\para\Damas-Cliente-Servidor\src
java jogo.DamasClient
```

| Terminal | Onde roda | Observação |
|----------|-----------|------------|
| 1º cliente | Qualquer PC na rede | Conecta ao IP configurado em `SERVER_ADDRESS` |
| 2º cliente | Outro PC **ou** segundo terminal no servidor | Mesma rede obrigatória |

Quando os dois clientes conectarem, o servidor inicia a partida e cada jogador recebe sua cor (**BRANCAS** ou **PRETAS**).

### 7. Jogar

- O tabuleiro é exibido no terminal após cada jogada.
- A mensagem `Vez das: BRANCAS` ou `Vez das: PRETAS` indica de quem é a vez.
- Para mover uma peça, digite **origem** e **destino** separados por espaço:

```
3A 4B
```

Isso move a peça da **linha 3, coluna A** para a **linha 4, coluna B**.

- Para sair: digite `SAIR`.

### Resumo rápido

```
[Servidor]  java jogo.DamasServer
[Cliente 1] java jogo.DamasClient
[Cliente 2] java jogo.DamasClient   (outro PC ou outro terminal)
```

---

## Solução de problemas

| Problema | O que verificar |
|----------|-----------------|
| `Não foi possível conectar ao servidor` | IPv4 correto em `SERVER_ADDRESS`? Servidor rodando? Mesma rede? |
| Firewall bloqueando | Liberar Java na porta **5555** |
| Jogo não começa | São necessários **dois** clientes conectados |
| `ERRO` ao mover | Formato `linhaColuna linhaColuna` (ex.: `3A 4B`); só jogue na sua vez |

---

## Protocolo de Aplicação

### Mensagens

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

### Fluxo do protocolo

1. **Conexão**: cliente conecta ao servidor na porta **5555**
2. **Aguardando**: servidor espera 2 clientes
3. **Início**: servidor envia `INICIO BRANCAS` e `INICIO PRETAS`
4. **Turno**: servidor envia `TURNO BRANCAS` ou `TURNO PRETAS`
5. **Jogada**: cliente envia `MOVIMENTO origem destino`
6. **Atualização**: servidor valida e envia `TABULEIRO` para ambos
7. **Fim**: servidor envia `VITORIA` quando alguém ganha

### Transporte

- **Protocolo**: TCP (Socket)
- **Porta**: 5555
- **Motivo**: garante entrega confiável e ordenada das mensagens

### Formato do tabuleiro

Serializado como: `vezBrancas;PEAO_BRANCO,VAZIO,DAMA_PRETA,...`

- Primeiro caractere: `1` = vez das brancas, `0` = vez das pretas
- Sequência de 64 peças separadas por vírgula
