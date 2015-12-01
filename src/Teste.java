
public class Teste {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
        int minId = 0;
        int maxId = 1;
        int randomNumber = -1;

       	randomNumber = (int)(Math.random()*(maxId - minId + 1)) + minId;

       	System.out.println("ID gerado: " + randomNumber);
	}

}
