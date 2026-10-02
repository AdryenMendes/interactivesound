import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class main {
    public static void main(String[] args) {
        
        // swing precisa rodar interface dentro de uma thread
        // invoker: janela só abra quando o java estiver 100%
        SwingUtilities.invokeLater(new Runnable() { 
            @Override
            public void run() {
                
                JFrame janela = new JFrame("janela de teste");

                janela.setSize(900, 600);

                // p o programa n rodar em 2 plano mesmo q a janela seja fechada
                janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                janela.setLocationRelativeTo(null);

                janela.setVisible(true);
            }
        });
    }
}