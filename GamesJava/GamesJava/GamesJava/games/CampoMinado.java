import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class CampoMinado extends JPanel implements MouseListener, KeyListener {
    private static final int LARGURA = 800;
    private static final int ALTURA = 600;

    enum Estado { MENU, JOGANDO, FIM_DE_JOGO, VITORIA }
    private Estado estadoAtual = Estado.MENU;

    // Configurações Variáveis de Dificuldade
    private int dificuldade = 1; // 1 = Fácil, 2 = Médio, 3 = Difícil
    private int linhas;
    private int colunas;
    private int totalMinas;
    private int tamanhoBloco;
    private int offsetX, offsetY;

    private boolean[][] minas;
    private boolean[][] revelado;
    private boolean[][] bandeiras;
    private int[][] vizinhos;

    private int blocosRevelados = 0;
    private int bandeirasRestantes = 0;
    private boolean primeiroClique = true;

    public CampoMinado() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(new Color(15, 15, 15));
        setFocusable(true);
        addMouseListener(this);
        addKeyListener(this);
    }

    private void configurarDificuldade() {
        if (dificuldade == 1) {
            linhas = 9; colunas = 9; totalMinas = 10;
        } else if (dificuldade == 2) {
            linhas = 16; colunas = 16; totalMinas = 40;
        } else {
            linhas = 16; colunas = 30; totalMinas = 99;
        }
    }

    private void iniciarJogo() {
        configurarDificuldade();

        // Calcula tamanho dinâmico para caber qualquer tabuleiro na tela
        tamanhoBloco = Math.min(LARGURA / colunas, (ALTURA - 80) / linhas);
        offsetX = (LARGURA - (colunas * tamanhoBloco)) / 2;
        offsetY = (ALTURA + 40 - (linhas * tamanhoBloco)) / 2;

        minas = new boolean[linhas][colunas];
        revelado = new boolean[linhas][colunas];
        bandeiras = new boolean[linhas][colunas];
        vizinhos = new int[linhas][colunas];

        blocosRevelados = 0;
        bandeirasRestantes = totalMinas;
        primeiroClique = true;

        estadoAtual = Estado.JOGANDO;
        repaint();
    }

    private void gerarMinasSeguro(int clickR, int clickC) {
        Random rand = new Random();
        int minasColocadas = 0;
        
        while (minasColocadas < totalMinas) {
            int r = rand.nextInt(linhas);
            int c = rand.nextInt(colunas);
            
            // Impede que a mina caia onde você clicou e nos 8 blocos ao redor
            boolean naAreaSegura = Math.abs(r - clickR) <= 1 && Math.abs(c - clickC) <= 1;

            if (!minas[r][c] && !naAreaSegura) {
                minas[r][c] = true;
                minasColocadas++;
            }
        }
        calcularVizinhos();
    }

    private void calcularVizinhos() {
        for (int r = 0; r < linhas; r++) {
            for (int c = 0; c < colunas; c++) {
                if (minas[r][c]) continue;
                
                int contagem = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int nr = r + i, nc = c + j;
                        if (nr >= 0 && nr < linhas && nc >= 0 && nc < colunas && minas[nr][nc]) {
                            contagem++;
                        }
                    }
                }
                vizinhos[r][c] = contagem;
            }
        }
    }

    private void revelarVazio(int r, int c) {
        if (r < 0 || r >= linhas || c < 0 || c >= colunas) return;
        if (revelado[r][c] || bandeiras[r][c]) return;

        revelado[r][c] = true;
        blocosRevelados++;

        // Se for 0 (nenhuma mina perto), revela os vizinhos
        if (vizinhos[r][c] == 0) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    revelarVazio(r + i, c + j);
                }
            }
        }
    }

    private void revelarVizinhosSeguros(int r, int c) {
        int bandeirasAoRedor = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int nr = r + i, nc = c + j;
                if (nr >= 0 && nr < linhas && nc >= 0 && nc < colunas && bandeiras[nr][nc]) {
                    bandeirasAoRedor++;
                }
            }
        }

        // Se as bandeiras batem com o número, revela o resto (Mecânica clássica de Chording)
        if (bandeirasAoRedor == vizinhos[r][c]) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int nr = r + i, nc = c + j;
                    if (nr >= 0 && nr < linhas && nc >= 0 && nc < colunas && !revelado[nr][nc] && !bandeiras[nr][nc]) {
                        if (minas[nr][nc]) {
                            revelarTudo();
                            estadoAtual = Estado.FIM_DE_JOGO;
                        } else {
                            revelarVazio(nr, nc);
                        }
                    }
                }
            }
            verificarVitoria();
        }
    }

    private void verificarVitoria() {
        if (blocosRevelados == (linhas * colunas) - totalMinas) {
            estadoAtual = Estado.VITORIA;
        }
    }

    private void revelarTudo() {
        for (int r = 0; r < linhas; r++) {
            for (int c = 0; c < colunas; c++) {
                revelado[r][c] = true;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (estadoAtual == Estado.MENU) {
            desenharMenu(g2d);
        } else {
            desenharJogo(g2d);
            if (estadoAtual == Estado.FIM_DE_JOGO) {
                desenharTelaFinal(g2d, "BOOM! GAME OVER", Color.RED);
            } else if (estadoAtual == Estado.VITORIA) {
                desenharTelaFinal(g2d, "VOCÊ VENCEU!", Color.GREEN);
            }
        }
    }

    private String getTextoDificuldade() {
        if (dificuldade == 1) return "FÁCIL (9x9, 10 minas)";
        if (dificuldade == 2) return "MÉDIO (16x16, 40 minas)";
        return "DIFÍCIL (16x30, 99 minas)";
    }

    private void desenharMenu(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 40));
        String titulo = "CAMPO MINADO";
        g.drawString(titulo, (LARGURA - g.getFontMetrics().stringWidth(titulo)) / 2, 160);

        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g.drawString("Clique Esquerdo: Revelar   |   Clique Direito: Bandeira", 100, 240);
        
        g.setColor(new Color(100, 200, 255));
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.drawString("DICA: Clique em um número aberto para revelar vizinhos mais rápido", 70, 275);

        // Seletor de Dificuldade
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        String dif = "< DIFICULDADE: " + getTextoDificuldade() + " >";
        g.drawString(dif, (LARGURA - g.getFontMetrics().stringWidth(dif)) / 2, 350);
        
        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        String setas = "(Use as Setas Esquerda/Direita para mudar)";
        g.drawString(setas, (LARGURA - g.getFontMetrics().stringWidth(setas)) / 2, 380);

        g.setColor(Color.GREEN);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        String msg = "Pressione ESPAÇO para iniciar";
        g.drawString(msg, (LARGURA - g.getFontMetrics().stringWidth(msg)) / 2, 470);

        g.setColor(Color.GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.drawString("[ ESC ] VOLTAR A CENTRAL", 280, 530);
    }

    private void desenharJogo(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.drawString("MINAS: " + bandeirasRestantes, offsetX, offsetY - 15);

        for (int r = 0; r < linhas; r++) {
            for (int c = 0; c < colunas; c++) {
                int px = offsetX + c * tamanhoBloco;
                int py = offsetY + r * tamanhoBloco;

                if (!revelado[r][c]) {
                    // Bloco fechado
                    g.setColor(new Color(100, 100, 100));
                    g.fillRect(px, py, tamanhoBloco, tamanhoBloco);
                    
                    int b3d = Math.max(2, tamanhoBloco / 8);
                    g.setColor(new Color(150, 150, 150));
                    g.fillRect(px, py, tamanhoBloco, b3d);
                    g.fillRect(px, py, b3d, tamanhoBloco);
                    g.setColor(new Color(60, 60, 60));
                    g.fillRect(px, py + tamanhoBloco - b3d, tamanhoBloco, b3d);
                    g.fillRect(px + tamanhoBloco - b3d, py, b3d, tamanhoBloco);

                    if (bandeiras[r][c]) {
                        g.setColor(Color.RED);
                        int[] tx = {px + (int)(tamanhoBloco*0.3), px + (int)(tamanhoBloco*0.7), px + (int)(tamanhoBloco*0.3)};
                        int[] ty = {py + (int)(tamanhoBloco*0.2), py + (int)(tamanhoBloco*0.4), py + (int)(tamanhoBloco*0.6)};
                        g.fillPolygon(tx, ty, 3);
                        g.setColor(Color.BLACK);
                        g.drawLine(px + (int)(tamanhoBloco*0.3), py + (int)(tamanhoBloco*0.2), px + (int)(tamanhoBloco*0.3), py + (int)(tamanhoBloco*0.8));
                    }
                } else {
                    // Bloco Aberto
                    g.setColor(new Color(40, 40, 40));
                    g.fillRect(px, py, tamanhoBloco, tamanhoBloco);
                    g.setColor(new Color(25, 25, 25));
                    g.drawRect(px, py, tamanhoBloco, tamanhoBloco);

                    if (minas[r][c]) {
                        g.setColor(Color.RED);
                        g.fillRect(px, py, tamanhoBloco, tamanhoBloco);
                        g.setColor(Color.BLACK);
                        int b = (int)(tamanhoBloco*0.2);
                        g.fillOval(px + b, py + b, tamanhoBloco - b*2, tamanhoBloco - b*2);
                    } else if (vizinhos[r][c] > 0) {
                        g.setFont(new Font("Monospaced", Font.BOLD, (int)(tamanhoBloco * 0.7)));
                        g.setColor(getCorNumero(vizinhos[r][c]));
                        String num = String.valueOf(vizinhos[r][c]);
                        FontMetrics fm = g.getFontMetrics();
                        int nx = px + (tamanhoBloco - fm.stringWidth(num)) / 2;
                        int ny = py + fm.getAscent() + (tamanhoBloco - fm.getHeight()) / 2;
                        g.drawString(num, nx, ny);
                    }
                }
            }
        }
    }

    private Color getCorNumero(int num) {
        switch (num) {
            case 1: return new Color(80, 150, 255);
            case 2: return new Color(80, 255, 80);
            case 3: return new Color(255, 80, 80);
            case 4: return new Color(180, 80, 255);
            case 5: return new Color(255, 255, 80);
            case 6: return new Color(80, 255, 255);
            case 7: return Color.WHITE;
            default: return Color.GRAY;
        }
    }

    private void desenharTelaFinal(Graphics2D g, String mensagem, Color cor) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, LARGURA, ALTURA);

        g.setColor(cor);
        g.setFont(new Font("Monospaced", Font.BOLD, 45));
        g.drawString(mensagem, (LARGURA - g.getFontMetrics().stringWidth(mensagem)) / 2, ALTURA / 2 - 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        String recomecar = "Pressione ESPAÇO para voltar ao Menu";
        g.drawString(recomecar, (LARGURA - g.getFontMetrics().stringWidth(recomecar)) / 2, ALTURA / 2 + 50);
    }

    // --- MOUSE ---
    @Override
    public void mouseReleased(MouseEvent e) {
        if (estadoAtual != Estado.JOGANDO) return;

        int mx = e.getX();
        int my = e.getY();

        if (mx >= offsetX && mx < offsetX + colunas * tamanhoBloco &&
            my >= offsetY && my < offsetY + linhas * tamanhoBloco) {
            
            int c = (mx - offsetX) / tamanhoBloco;
            int r = (my - offsetY) / tamanhoBloco;

            if (SwingUtilities.isRightMouseButton(e)) {
                if (!revelado[r][c]) {
                    bandeiras[r][c] = !bandeiras[r][c];
                    bandeirasRestantes += bandeiras[r][c] ? -1 : 1;
                }
            } else if (SwingUtilities.isLeftMouseButton(e)) {
                if (!revelado[r][c] && !bandeiras[r][c]) {
                    if (primeiroClique) {
                        gerarMinasSeguro(r, c); // Garante que o 1º clique é salvo
                        primeiroClique = false;
                    }

                    if (minas[r][c]) {
                        revelarTudo();
                        estadoAtual = Estado.FIM_DE_JOGO;
                    } else {
                        revelarVazio(r, c);
                        verificarVitoria();
                    }
                } else if (revelado[r][c] && vizinhos[r][c] > 0) {
                    // Clique em um número para abrir os arredores (Chording)
                    revelarVizinhosSeguros(r, c);
                }
            }
            repaint();
        }
    }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // --- TECLADO ---
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (estadoAtual == Estado.MENU) {
            if (key == KeyEvent.VK_LEFT) {
                dificuldade = Math.max(1, dificuldade - 1);
            }
            if (key == KeyEvent.VK_RIGHT) {
                dificuldade = Math.min(3, dificuldade + 1);
            }
            if (key == KeyEvent.VK_SPACE) iniciarJogo();
            if (key == KeyEvent.VK_ESCAPE) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame != null) frame.dispose();
            }
        } else if (estadoAtual == Estado.JOGANDO) {
            if (key == KeyEvent.VK_ESCAPE) estadoAtual = Estado.MENU;
        } else {
            if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_ESCAPE) estadoAtual = Estado.MENU;
        }
        repaint();
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}