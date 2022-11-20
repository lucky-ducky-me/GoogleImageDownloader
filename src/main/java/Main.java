import java.util.Scanner;

public class Main {
    static final String savingDir = "\"E:\\\\downloadedImgs\"";

    static public void main(String[] args) {
        var first = true;

        while (true) {
            var console = new Scanner(System.in);

            if (!first) {
                System.out.println("Закончить работу? [y/n]");

                var answer = console.nextLine();

                if (answer.equals("y") || answer.equals("Y")) {
                    break;
                }
            }

            first = false;

            var searchingWordMessage = "Введите слово для поиска картинок:    ";

            System.out.println(searchingWordMessage);

            var searchingWord = console.nextLine();

            var imagesAmountMessage = "Введите количество скачиваемых картинок:    ";

            System.out.println(imagesAmountMessage);

            var imagesAmount = -1;

            try {
                imagesAmount = Integer.parseInt(console.nextLine());
            }
            catch (NumberFormatException ex) {
                System.out.println("Введите корректное число.");
                continue;
            }

            try {

                var finalImagesAmount = imagesAmount;

                var thread = new Thread(() -> {
                    var loader = new GoogleImageDownloader();

                    loader.saveImagesInFile(searchingWord, finalImagesAmount, savingDir);
                }, "thread");

                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
