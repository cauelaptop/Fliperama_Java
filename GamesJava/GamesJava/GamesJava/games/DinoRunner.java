import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class DinoRunner extends JPanel implements ActionListener, KeyListener {
    private static final int LARGURA = 800;
    private static final int ALTURA = 600;
    private static final int CHAO = 450;

    enum Estado { MENU, JOGANDO, GAMEOVER }
    private Estado estadoAtual = Estado.MENU;
    private int dificuldade = 1; // 1 = Fácil, 2 = Médio, 3 = Difícil

    private Timer timer;
    private int pontuacao;
    private int recorde = 0;
    private int velocidadeCenario;
    private int frameCactus = 0;

    // Dinossauro
    private int dinoX = 100, dinoY = CHAO - 40;
    private int dinoLargura = 40, dinoAltura = 40;
    private double velocidadeY = 0;
    private final double GRAVIDADE = 1.0;
    private final double FORCA_PULO = -16.0;
    private boolean abaixado = false;

    // Obstáculos (Cactos)
    private ArrayList<Rectangle> obstaculos;
    
    // Nuvens para decoração
    private ArrayList<Rectangle> nuvens;

    public DinoRunner() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(new Color(240, 240, 240));
        setFocusable(true);
        addKeyListener(this);

        obstaculos = new ArrayList<>();
        nuvens = new ArrayList<>();
        timer = new Timer(20, this);
        gerarNuvensIniciais();
    }

    private void iniciarJogo() {
        pontuacao = 0;
        dinoY = CHAO - dinoAltura;
        velocidadeY = 0;
        abaixado = false;
        obstaculos.clear();
        
        if (dificuldade == 1) velocidadeCenario = 6;
        else if (dificuldade == 2) velocidadeCenario = 9;
        else velocidadeCenario = 13;

        estadoAtual = Estado.JOGANDO;
        timer.start();
    }

    private void gerarNuvensIniciais() {
        nuvens.clear();
        for (int i = 0; i < 5; i++) {
            int nx = (int) (Math.random() * LARGURA);
            int ny = 50 + (int) (Math.random() * 150);
            nuvens.add(new Rectangle(nx, ny, 60, 30));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (estadoAtual == Estado.JOGANDO) {
            atualizarFisica();
            atualizarCenario();
            verificarColisao();
            
            pontuacao++;
            if (pontuacao % 500 == 0) {
                velocidadeCenario++; // Fica mais rápido com o tempo
            }
            repaint();
        }
    }

    private void atualizarFisica() {
        dinoY += velocidadeY;
        velocidadeY += GRAVIDADE;

        // Limite do chão dependendo se está abaixado
        int alturaAtual = abaixado ? dinoAltura / 2 : dinoAltura;
        if (dinoY + alturaAtual >= CHAO) {
            dinoY = CHAO - alturaAtual;
            velocidadeY = 0;
        }
    }

    private void atualizarCenario() {
        // Nuvens
        for (Rectangle nuvem : nuvens) {
            nuvem.x -= velocidadeCenario / 3; // Nuvens movem mais devagar (Parallax)
        }
        if (!nuvens.isEmpty() && nuvens.get(0).x + nuvens.get(0).width < 0) {
            nuvens.remove(0);
            nuvens.add(new Rectangle(LARGURA + (int)(Math.random() * 200), 50 + (int) (Math.random() * 150), 60, 30));
        }

        // Cactos
        for (Rectangle obs : obstaculos) {
            obs.x -= velocidadeCenario;
        }
        if (!obstaculos.isEmpty() && obstaculos.get(0).x + obstaculos.get(0).width < 0) {
            obstaculos.remove(0);
        }

        // Spawn de novos cactos
        frameCactus++;
        int frequencia = dificuldade == 1 ? 80 : (dificuldade == 2 ? 60 : 45);
        if (frameCactus > frequencia && Math.random() < 0.05) {
            int alturaCactus = 30 + (int) (Math.random() * 30);
            int larguraCactus = 20 + (int) (Math.random() * 15);
            obstaculos.add(new Rectangle(LARGURA, CHAO - alturaCactus, larguraCactus, alturaCactus));
            frameCactus = 0;
        }
    }

    private void verificarColisao() {
        int alturaAtual = abaixado ? dinoAltura / 2 : dinoAltura;
        Rectangle caixaDino = new Rectangle(dinoX + 5, dinoY + 5, dinoLargura - 10, alturaAtual - 10); // Hitbox levemente menor

        for (Rectangle obs : obstaculos) {
            if (caixaDino.intersects(obs)) {
                estadoAtual = Estado.GAMEOVER;
                timer.stop();
                if (pontuacao > recorde) recorde = pontuacao;
                break;
            }
        }
    }

    // --- RENDERIZAÇÃO ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (estadoAtual == Estado.MENU) {
            desenharMenu(g2d);
        } else {
            desenharJogo(g2d);
            if (estadoAtual == Estado.GAMEOVER) {
                desenharGameOver(g2d);
            }
        }
    }

    private void desenharMenu(Graphics2D g) {
        setBackground(new Color(40, 45, 50));
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 50));
        g.drawString("DINO RUNNER", LARGURA / 2 - 160, 180);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        String dif = "< DIFICULDADE: " + (dificuldade == 1 ? "FÁCIL" : dificuldade == 2 ? "MÉDIO" : "DIFÍCIL") + " >";
        g.drawString(dif, (LARGURA - g.getFontMetrics().stringWidth(dif)) / 2, 300);
        
        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g.drawString("(Setas Esq/Dir para mudar)", LARGURA / 2 - 110, 330);

        g.setColor(Color.GREEN);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.drawString("Pressione ESPAÇO para Iniciar", LARGURA / 2 - 170, 420);
        
        g.setColor(Color.GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.drawString("[ ESC ] VOLTAR A CENTRAL", LARGURA / 2 - 115, 480);
    }

    private void desenharJogo(Graphics2D g) {
        setBackground(new Color(240, 240, 240));

        // Nuvens
        g.setColor(new Color(200, 200, 200));
        for (Rectangle n : nuvens) {
            g.fillRoundRect(n.x, n.y, n.width, n.height, 20, 20);
            g.fillOval(n.x + 10, n.y - 10, 30, 30);
            g.fillOval(n.x + 30, n.y - 5, 25, 25);
        }

        // Chão
        g.setColor(new Color(80, 80, 80));
        g.setStroke(new BasicStroke(3));
        g.drawLine(0, CHAO, LARGURA, CHAO);

        // Cactos
        g.setColor(new Color(34, 139, 34)); // Verde floresta
        for (Rectangle obs : obstaculos) {
            g.fillRoundRect(obs.x, obs.y, obs.width, obs.height, 5, 5);
        }

        // Dinossauro
        g.setColor(new Color(83, 83, 83)); // Cinza escuro
        int alturaAtual = abaixado ? dinoAltura / 2 : dinoAltura;
        g.fillRoundRect(dinoX, dinoY, dinoLargura, alturaAtual, 8, 8);
        
        // Olhinho do dino
        g.setColor(Color.WHITE);
        g.fillRect(dinoX + dinoLargura - 12, dinoY + 5, 4, 4);

        // Placar
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        String textoPlacar = String.format("HI %05d  %05d", recorde, pontuacao / 10);
        g.drawString(textoPlacar, LARGURA - 250, 40);
    }

    private void desenharGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, LARGURA, ALTURA);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 45));
        String msg = "G A M E  O V E R";
        g.drawString(msg, (LARGURA - g.getFontMetrics().stringWidth(msg)) / 2, ALTURA / 2 - 20);

        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        String recomecar = "Pressione ESPAÇO para reiniciar";
        g.drawString(recomecar, (LARGURA - g.getFontMetrics().stringWidth(recomecar)) / 2, ALTURA / 2 + 30);
        
        String menu = "Pressione ESC para voltar ao Menu";
        g.drawString(menu, (LARGURA - g.getFontMetrics().stringWidth(menu)) / 2, ALTURA / 2 + 60);
    }

    // --- CONTROLES TECLADO ---
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (estadoAtual == Estado.MENU) {
            if (key == KeyEvent.VK_LEFT) dificuldade = Math.max(1, dificuldade - 1);
            if (key == KeyEvent.VK_RIGHT) dificuldade = Math.min(3, dificuldade + 1);
            if (key == KeyEvent.VK_SPACE) iniciarJogo();
            if (key == KeyEvent.VK_ESCAPE) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame != null) frame.dispose();
            }
            repaint();
        } else if (estadoAtual == Estado.JOGANDO) {
            if ((key == KeyEvent.VK_SPACE || key == KeyEvent.VK_UP) && dinoY + (abaixado ? dinoAltura/2 : dinoAltura) >= CHAO) {
                velocidadeY = FORCA_PULO;
                abaixado = false;
            }
            if (key == KeyEvent.VK_DOWN) {
                abaixado = true;
                velocidadeY += 2.0; // Cai mais rápido se apertar pra baixo no ar
            }
        } else if (estadoAtual == Estado.GAMEOVER) {
            if (key == KeyEvent.VK_SPACE) iniciarJogo();
            if (key == KeyEvent.VK_ESCAPE) {
                estadoAtual = Estado.MENU;
                repaint();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            abaixado = false;
            if (dinoY + dinoAltura > CHAO) dinoY = CHAO - dinoAltura; // Corrige posição ao levantar
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
}