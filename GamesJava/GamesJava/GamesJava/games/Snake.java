import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class Snake extends JPanel implements KeyListener, ActionListener {
    private static final int LARGURA = 800;
    private static final int ALTURA = 600;
    private static final int TAMANHO_BLOCO = 25;
    private static final int UNIDADES = (LARGURA * ALTURA) / (TAMANHO_BLOCO * TAMANHO_BLOCO);

    enum Estado { MENU, JOGANDO, FIM_DE_JOGO }
    private Estado estadoAtual = Estado.MENU;

    // Corpo da cobra
    private final int[] x = new int[UNIDADES];
    private final int[] y = new int[UNIDADES];
    private int tamanhoCobra = 3;

    // Direção: 'C' (Cima), 'B' (Baixo), 'E' (Esquerda), 'D' (Direita)
    private char direcao = 'D';
    private char novaDirecao = 'D'; // Evita múltiplas mudanças na mesma iteração

    // Maçã
    private int macaX;
    private int macaY;

    // Jogo
    private int pontos = 0;
    private int atraso = 120; // Velocidade (menor = mais rápido)
    private Timer timer;
    private Random random;

    public Snake() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(new Color(15, 15, 15));
        setFocusable(true);
        addKeyListener(this);
        random = new Random();
        
        timer = new Timer(atraso, this);
        timer.start();
    }

    private void iniciarJogo() {
        tamanhoCobra = 3;
        pontos = 0;
        direcao = 'D';
        novaDirecao = 'D';

        // Posição inicial
        for (int i = 0; i < tamanhoCobra; i++) {
            x[i] = LARGURA / 2 - (i * TAMANHO_BLOCO);
            y[i] = ALTURA / 2;
        }

        gerarMaca();
        estadoAtual = Estado.JOGANDO;
        timer.setDelay(atraso); // Resetar velocidade
    }

    private void gerarMaca() {
        boolean posicaoValida;
        do {
            posicaoValida = true;
            macaX = random.nextInt((int) (LARGURA / TAMANHO_BLOCO)) * TAMANHO_BLOCO;
            macaY = random.nextInt((int) (ALTURA / TAMANHO_BLOCO)) * TAMANHO_BLOCO;

            // Garantir que a maçã não apareça dentro da cobra
            for (int i = 0; i < tamanhoCobra; i++) {
                if (x[i] == macaX && y[i] == macaY) {
                    posicaoValida = false;
                    break;
                }
            }
        } while (!posicaoValida);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (estadoAtual == Estado.JOGANDO) {
            mover();
            verificarMaca();
            verificarColisoes();
        }
        repaint();
    }

    private void mover() {
        // Atualiza a direção atual baseada no último input válido
        direcao = novaDirecao;

        // Move o corpo
        for (int i = tamanhoCobra; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        // Move a cabeça
        switch (direcao) {
            case 'C': y[0] -= TAMANHO_BLOCO; break;
            case 'B': y[0] += TAMANHO_BLOCO; break;
            case 'E': x[0] -= TAMANHO_BLOCO; break;
            case 'D': x[0] += TAMANHO_BLOCO; break;
        }
    }

    private void verificarMaca() {
        if ((x[0] == macaX) && (y[0] == macaY)) {
            tamanhoCobra++;
            pontos += 10;
            gerarMaca();
            
            // Aumentar a velocidade levemente a cada maçã
            int novoAtraso = timer.getDelay() - 2;
            if (novoAtraso > 40) {
                timer.setDelay(novoAtraso);
            }
        }
    }

    private void verificarColisoes() {
        // Bateu no próprio corpo
        for (int i = tamanhoCobra; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                estadoAtual = Estado.FIM_DE_JOGO;
                return;
            }
        }

        // Bateu nas bordas
        if (x[0] < 0 || x[0] >= LARGURA || y[0] < 0 || y[0] >= ALTURA) {
            estadoAtual = Estado.FIM_DE_JOGO;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (estadoAtual == Estado.MENU) {
            desenharMenu(g2d);
        } else if (estadoAtual == Estado.JOGANDO) {
            desenharJogo(g2d);
        } else {
            desenharJogo(g2d); // Mantém o jogo desenhado ao fundo
            desenharTelaFinal(g2d);
        }
    }

    private void desenharMenu(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 40));
        String titulo = "SNAKE ARCADE";
        g.drawString(titulo, (LARGURA - g.getFontMetrics().stringWidth(titulo)) / 2, 200);

        g.setColor(Color.GREEN);
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        String msg = "Pressione ESPAÇO para iniciar";
        g.drawString(msg, (LARGURA - g.getFontMetrics().stringWidth(msg)) / 2, 300);

        g.setColor(Color.GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        String sair = "[ ESC ] VOLTAR A CENTRAL";
        g.drawString(sair, (LARGURA - g.getFontMetrics().stringWidth(sair)) / 2, 450);
    }

    private void desenharJogo(Graphics2D g) {
        // Linhas de grade sutis (Opcional)
        g.setColor(new Color(30, 30, 30));
        for (int i = 0; i < LARGURA / TAMANHO_BLOCO; i++) {
            g.drawLine(i * TAMANHO_BLOCO, 0, i * TAMANHO_BLOCO, ALTURA);
        }
        for (int i = 0; i < ALTURA / TAMANHO_BLOCO; i++) {
            g.drawLine(0, i * TAMANHO_BLOCO, LARGURA, i * TAMANHO_BLOCO);
        }

        // Maçã
        g.setColor(new Color(255, 50, 50));
        g.fillOval(macaX + 2, macaY + 2, TAMANHO_BLOCO - 4, TAMANHO_BLOCO - 4);

        // Cobra
        for (int i = 0; i < tamanhoCobra; i++) {
            if (i == 0) {
                g.setColor(new Color(50, 255, 50)); // Cabeça mais clara
            } else {
                g.setColor(new Color(0, 180, 0)); // Corpo
            }
            g.fillRoundRect(x[i] + 1, y[i] + 1, TAMANHO_BLOCO - 2, TAMANHO_BLOCO - 2, 8, 8);
        }

        // Placar
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.drawString("SCORE: " + pontos, 20, 30);
    }

    private void desenharTelaFinal(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, LARGURA, ALTURA);

        g.setColor(Color.RED);
        g.setFont(new Font("Monospaced", Font.BOLD, 50));
        String txt = "GAME OVER";
        g.drawString(txt, (LARGURA - g.getFontMetrics().stringWidth(txt)) / 2, ALTURA / 2 - 30);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        String pts = "Pontuação: " + pontos;
        g.drawString(pts, (LARGURA - g.getFontMetrics().stringWidth(pts)) / 2, ALTURA / 2 + 20);

        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        String recomecar = "Pressione ESPAÇO para jogar novamente";
        g.drawString(recomecar, (LARGURA - g.getFontMetrics().stringWidth(recomecar)) / 2, ALTURA / 2 + 70);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (estadoAtual == Estado.MENU) {
            if (key == KeyEvent.VK_SPACE) iniciarJogo();
            if (key == KeyEvent.VK_ESCAPE) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame != null) frame.dispose();
            }
        } else if (estadoAtual == Estado.JOGANDO) {
            // Evita voltar para trás (ex: ir pra direita se já está indo pra esquerda)
            if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) && direcao != 'D') {
                novaDirecao = 'E';
            } else if ((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) && direcao != 'E') {
                novaDirecao = 'D';
            } else if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_W) && direcao != 'B') {
                novaDirecao = 'C';
            } else if ((key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) && direcao != 'C') {
                novaDirecao = 'B';
            }
        } else if (estadoAtual == Estado.FIM_DE_JOGO) {
            if (key == KeyEvent.VK_SPACE) iniciarJogo();
            if (key == KeyEvent.VK_ESCAPE) estadoAtual = Estado.MENU;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}