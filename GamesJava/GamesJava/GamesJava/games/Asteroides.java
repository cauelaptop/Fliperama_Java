import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class Asteroides extends JPanel implements ActionListener, KeyListener {
    private static final int LARGURA = 800;
    private static final int ALTURA = 600;

    enum Estado { MENU, JOGANDO, GAMEOVER }
    private Estado estadoAtual = Estado.MENU;

    private Timer timer;
    private Random random = new Random();

    // Controles
    private boolean girarEsq = false, girarDir = false, acelerando = false;

    // Entidades
    private Nave nave;
    private List<Tiro> tiros;
    private List<Asteroide> asteroides;
    
    private int pontuacao;

    public Asteroides() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        timer = new Timer(16, this); // ~60 FPS
        timer.start();
    }

    private void iniciarJogo() {
        nave = new Nave(LARGURA / 2.0, ALTURA / 2.0);
        tiros = new ArrayList<>();
        asteroides = new ArrayList<>();
        pontuacao = 0;
        
        girarEsq = false;
        girarDir = false;
        acelerando = false;

        // Criar asteroides iniciais
        for (int i = 0; i < 4; i++) {
            criarAsteroideLongeDaNave();
        }

        estadoAtual = Estado.JOGANDO;
    }

    private void criarAsteroideLongeDaNave() {
        double ax, ay;
        do {
            ax = random.nextDouble() * LARGURA;
            ay = random.nextDouble() * ALTURA;
        } while (Math.hypot(ax - nave.x, ay - nave.y) < 150); // Garante que não nasça em cima da nave
        asteroides.add(new Asteroide(ax, ay, 3));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (estadoAtual == Estado.JOGANDO) {
            atualizarJogo();
        }
        repaint();
    }

    private void atualizarJogo() {
        // Atualizar Nave
        if (girarEsq) nave.angulo -= 0.1;
        if (girarDir) nave.angulo += 0.1;
        if (acelerando) nave.acelerar();
        nave.atualizar();

        // Atualizar Tiros
        Iterator<Tiro> itTiro = tiros.iterator();
        while (itTiro.hasNext()) {
            Tiro t = itTiro.next();
            t.atualizar();
            if (t.vida <= 0) itTiro.remove();
        }

        // Atualizar Asteroides e Checar Colisões
        boolean asteroideDestruido = false;
        Iterator<Asteroide> itAst = asteroides.iterator();
        List<Asteroide> novosAsteroides = new ArrayList<>();

        while (itAst.hasNext()) {
            Asteroide a = itAst.next();
            a.atualizar();

            // Colisão Asteroide x Nave
            if (Math.hypot(a.x - nave.x, a.y - nave.y) < a.raio + 10) {
                estadoAtual = Estado.GAMEOVER;
            }

            // Colisão Asteroide x Tiros
            Iterator<Tiro> itT = tiros.iterator();
            while (itT.hasNext()) {
                Tiro t = itT.next();
                if (Math.hypot(a.x - t.x, a.y - t.y) < a.raio) {
                    pontuacao += (4 - a.tamanho) * 100;
                    itT.remove();
                    itAst.remove();
                    asteroideDestruido = true;
                    
                    // Quebra o asteroide em pedaços menores
                    if (a.tamanho > 1) {
                        novosAsteroides.add(new Asteroide(a.x, a.y, a.tamanho - 1));
                        novosAsteroides.add(new Asteroide(a.x, a.y, a.tamanho - 1));
                    }
                    break;
                }
            }
        }
        asteroides.addAll(novosAsteroides);

        // Se limpou a tela, adiciona mais asteroides (aumentando a dificuldade)
        if (asteroides.isEmpty() && asteroideDestruido) {
            int qtd = 4 + (pontuacao / 1000);
            for (int i = 0; i < qtd; i++) criarAsteroideLongeDaNave();
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
        } else if (estadoAtual == Estado.GAMEOVER) {
            desenharJogo(g2d);
            desenharGameOver(g2d);
        }
    }

    private void desenharMenu(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 50));
        g.drawString("ASTEROIDES", LARGURA / 2 - 145, 200);

        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("Mova: W A D ou Setinhas", LARGURA / 2 - 140, 300);
        g.drawString("Atire: ESPAÇO", LARGURA / 2 - 80, 330);

        g.setColor(Color.GREEN);
        g.drawString("Pressione ESPAÇO para Iniciar", LARGURA / 2 - 170, 420);
        
        g.setColor(Color.GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.drawString("[ ESC ] VOLTAR A CENTRAL", LARGURA / 2 - 115, 480);
    }

    private void desenharJogo(Graphics2D g) {
        // Pontuação
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.drawString("SCORE: " + pontuacao, 20, 30);

        // Desenhar Tiros
        g.setColor(Color.YELLOW);
        for (Tiro t : tiros) {
            g.fillOval((int)t.x - 2, (int)t.y - 2, 4, 4);
        }

        // Desenhar Asteroides
        g.setColor(Color.LIGHT_GRAY);
        g.setStroke(new BasicStroke(2));
        for (Asteroide a : asteroides) {
            g.drawOval((int)(a.x - a.raio), (int)(a.y - a.raio), a.raio * 2, a.raio * 2);
        }

        // Desenhar Nave
        nave.desenhar(g);
    }

    private void desenharGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, LARGURA, ALTURA);

        g.setColor(Color.RED);
        g.setFont(new Font("Monospaced", Font.BOLD, 50));
        g.drawString("GAME OVER", LARGURA / 2 - 130, ALTURA / 2 - 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g.drawString("Pressione ESPAÇO para tentar de novo", LARGURA / 2 - 210, ALTURA / 2 + 30);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (estadoAtual == Estado.MENU) {
            if (key == KeyEvent.VK_SPACE) iniciarJogo();
            if (key == KeyEvent.VK_ESCAPE) fecharJanela();
        } 
        else if (estadoAtual == Estado.JOGANDO) {
            // Controles de Rotação (A/D ou Setas Esq/Dir)
            if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) girarEsq = true;
            if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) girarDir = true;
            
            // Controle de Aceleração (W ou Seta Cima)
            if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) acelerando = true;
            
            // Tiro
            if (key == KeyEvent.VK_SPACE) {
                if (tiros.size() < 10) { // Limite de tiros na tela
                    tiros.add(new Tiro(nave.x, nave.y, nave.angulo));
                }
            }
            if (key == KeyEvent.VK_ESCAPE) estadoAtual = Estado.MENU;
        } 
        else if (estadoAtual == Estado.GAMEOVER) {
            if (key == KeyEvent.VK_SPACE) iniciarJogo();
            if (key == KeyEvent.VK_ESCAPE) estadoAtual = Estado.MENU;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) girarEsq = false;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) girarDir = false;
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) acelerando = false;
    }

    @Override public void keyTyped(KeyEvent e) {}

    private void fecharJanela() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame != null) frame.dispose();
    }

    // --- CLASSES INTERNAS (FÍSICA E ENTIDADES) ---

    class Nave {
        double x, y;
        double vx, vy;
        double angulo; // em radianos

        public Nave(double x, double y) {
            this.x = x; this.y = y;
            this.vx = 0; this.vy = 0;
            this.angulo = -Math.PI / 2; // Apontando para cima
        }

        public void acelerar() {
            vx += Math.cos(angulo) * 0.2;
            vy += Math.sin(angulo) * 0.2;
            
            // Limitar velocidade
            double vel = Math.hypot(vx, vy);
            if (vel > 6) {
                vx = (vx / vel) * 6;
                vy = (vy / vel) * 6;
            }
        }

        public void atualizar() {
            x += vx;
            y += vy;
            // Atrito para a nave parar devagar
            vx *= 0.99;
            vy *= 0.99;

            envolverTela();
        }

        private void envolverTela() {
            if (x < 0) x += LARGURA;
            if (x > LARGURA) x -= LARGURA;
            if (y < 0) y += ALTURA;
            if (y > ALTURA) y -= ALTURA;
        }

        public void desenhar(Graphics2D g) {
            int cx = (int) x, cy = (int) y;
            int[] xPoints = {
                cx + (int)(20 * Math.cos(angulo)),
                cx + (int)(15 * Math.cos(angulo + 2.4)),
                cx + (int)(15 * Math.cos(angulo - 2.4))
            };
            int[] yPoints = {
                cy + (int)(20 * Math.sin(angulo)),
                cy + (int)(15 * Math.sin(angulo + 2.4)),
                cy + (int)(15 * Math.sin(angulo - 2.4))
            };

            g.setColor(Color.WHITE);
            g.drawPolygon(xPoints, yPoints, 3);
            
            // Fogo da turbina
            if (acelerando) {
                g.setColor(Color.ORANGE);
                int[] fogoX = {
                    cx + (int)(15 * Math.cos(angulo + 2.8)),
                    cx + (int)(25 * Math.cos(angulo + Math.PI)),
                    cx + (int)(15 * Math.cos(angulo - 2.8))
                };
                int[] fogoY = {
                    cy + (int)(15 * Math.sin(angulo + 2.8)),
                    cy + (int)(25 * Math.sin(angulo + Math.PI)),
                    cy + (int)(15 * Math.sin(angulo - 2.8))
                };
                g.drawPolygon(fogoX, fogoY, 3);
            }
        }
    }

    class Tiro {
        double x, y, vx, vy;
        int vida = 60; // Duração do tiro em frames

        public Tiro(double x, double y, double angulo) {
            this.x = x + 20 * Math.cos(angulo); // Sai da ponta da nave
            this.y = y + 20 * Math.sin(angulo);
            this.vx = Math.cos(angulo) * 10;
            this.vy = Math.sin(angulo) * 10;
        }

        public void atualizar() {
            x += vx;
            y += vy;
            vida--;

            if (x < 0) x += LARGURA;
            if (x > LARGURA) x -= LARGURA;
            if (y < 0) y += ALTURA;
            if (y > ALTURA) y -= ALTURA;
        }
    }

    class Asteroide {
        double x, y, vx, vy;
        int tamanho; // 3 = Grande, 2 = Médio, 1 = Pequeno
        int raio;

        public Asteroide(double x, double y, int tamanho) {
            this.x = x; this.y = y;
            this.tamanho = tamanho;
            this.raio = tamanho * 15;
            
            double ang = random.nextDouble() * 2 * Math.PI;
            double velocidade = random.nextDouble() * 1.5 + 0.5 + (3 - tamanho); // Menores são mais rápidos
            this.vx = Math.cos(ang) * velocidade;
            this.vy = Math.sin(ang) * velocidade;
        }

        public void atualizar() {
            x += vx;
            y += vy;

            if (x < -raio) x = LARGURA + raio;
            if (x > LARGURA + raio) x = -raio;
            if (y < -raio) y = ALTURA + raio;
            if (y > ALTURA + raio) y = -raio;
        }
    }
}