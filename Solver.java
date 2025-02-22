import java.io.*;
import java.util.*;



public class Solver {
    private static int rows, cols, count_piece;
    private static String type_board;
    private static char[][] board;
    private static List<char[][]> pieces = new ArrayList<>();
    private static long iterationCount;
    private static char [][] piece;
    private static final String[] color = {
        "\u001B[31m", // Red
        "\u001B[32m", // Green
        "\u001B[33m", // Yellow
        "\u001B[34m", // Blue
        "\u001B[35m", // Magenta
        "\u001B[36m", // Cyan
        "\u001B[38;5;208m", // Orange
        "\u001B[38;5;214m", // Light Orange
        "\u001B[38;5;165m", // Purple
        "\u001B[38;5;200m", // Pink
        "\u001B[38;5;118m", // Bright Lime Green
        "\u001B[38;5;75m",  // Sky Blue
        "\u001B[38;5;220m", // Gold
        "\u001B[38;5;130m", // Brown
        "\u001B[38;5;255m", // Light Gray
        "\u001B[38;5;21m",  // Deep Blue
        "\u001B[91m", // Bright Red
        "\u001B[92m", // Bright Green
        "\u001B[93m", // Bright Yellow
        "\u001B[94m", // Bright Blue
        "\u001B[95m", // Bright Magenta
        "\u001B[96m", // Bright Cyan
        "\u001B[90m", // Dark Gray
        "\u001B[97m",  // White
        "\u001B[38;5;196m", // Dark Red
        "\u001B[38;5;46m",  // Dark Green
    };


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Masukkan path file(.txt): ");
        String fileName = scanner.nextLine();


        if (!cekReadFile(fileName)) {
            System.out.println("File tidak valid!");
            return;
        }


    }





    public static boolean cekReadFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String[] info = reader.readLine().split(" ");
            
            if (info[0]== null||info[1]==null || info[2]==null)return false;

            rows = Integer.parseInt(info[0]);
            cols = Integer.parseInt(info[1]);
            count_piece = Integer.parseInt(info[2]);


            type_board = reader.readLine().trim();
            if (!type_board.equals("DEFAULT") && !type_board.equals("PIRAMID") && !type_board.equals("CUSTOM")) {
                return false;
            }

            board =new char[rows][cols];
            for (char[] row : board){
                Arrays.fill(row,'.');
            }  


            int tes_count_piece = 0;
            String temp = "";       //huruf sementara yang disimpan 
            String line;
            List<String> shapeLine = new ArrayList<>();



            while ((line = reader.readLine()) != null) {
                line = line.stripTrailing();    // delete space after piece
                if( line.isEmpty()){
                    continue;                   //skip empty line
                }

                char firstChar = line.trim().charAt(0);    //mengambil char pertama (bukan spasi)

                if ( firstChar== temp.charAt(0) || temp.isEmpty() ){  // jika karakter masi sama atau pertama
                    shapeLine.add(line);
                }else{
                    pieces.add(convertToMatriks(shapeLine, temp.charAt(0)));//karakter baru,karakter lama diubah ke matriks
                    tes_count_piece++;

                    shapeLine.clear(); // reset shapeline
                    shapeLine.add(line);
                
                }   
                temp = String.valueOf(firstChar);
                
            }
            if (!shapeLine.isEmpty()) { // last piece
                pieces.add(convertToMatriks(shapeLine, temp.charAt(0)));
                tes_count_piece++;
            }

            return tes_count_piece == count_piece;
        } catch (IOException e) {
            System.out.println("Error membaca file!");
            return false;
        }
    }


    public static char[][] convertToMatriks(List<String> shapeLine, char letter){
        int tinggi, lebar ;
        tinggi = shapeLine.size();
        lebar = shapeLine.stream().mapToInt(String::length).max().orElse(0);// find max long

        char[][] shape = new char[tinggi][lebar];


        for (int i = 0; i < tinggi; i++) {
            Arrays.fill(shape[i], ' ');    // spasi
            String line = shapeLine.get(i);  // ambil baris
                    
            for (int j = 0; j < line.length(); j++) {
                if (line.charAt(j) == letter) {
                    shape[i][j] = letter; 
                }
            }
        }
        return shape;

    }

    public static char[][] copyMatriks ( char[][] matriks){
        int r = matriks.length;
        int c = matriks[0].length;
        char[][] copy = new char[rows][cols];
    
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                copy[i][j] = matriks[i][j];
            }
        }
        return copy;
    }


    public static void printMatriks(char[][] matriks){
        for (char[] row : matriks){
            for (char isi :row){
                System.out.print(isi == ' ' ? '.' : isi); // jika spasi maka jadi titik
            }
            System.out.println();
        }
    }


    public static boolean solve (int pieceIndex){
        if (pieceIndex >= pieces.size()){
            return true;
        }
        
        System.out.println("Trying piece index: " + pieceIndex);
        
        piece = pieces.get(pieceIndex);

        //-------------
        // generate

        //---------------------
        Set<String> uniquePiece = new HashSet<>();
        List<char[][]> variations = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            piece = rotate90(piece);        //rotasi dicek 4 kali

            if (uniquePiece.add(Arrays.deepToString(piece))) {
                variations.add(copyMatrix(piece));  // tambahkan variasi dari rotate
            }

            char[][] mirroredPiece = mirror(piece); // mirror hasil rotasi
            if (uniquePiece.add(Arrays.deepToString(mirroredPiece))) {
                variations.add(copyMatriks(mirroredPiece)); // tambah hasil pencerminan
            }


        }

        
        for (char[][] variant : variations) {
            for (int row = 0; row <= rows - variant.length; row++) {
                for (int col = 0; col <= cols - variant[0].length; col++) {
                    if (canPlace(variant, row, col)) {
                        
                        // Menentukan simbol potongan (pieceSymbol), menghindari spasi
                        char pieceSymbol = piece[0][0];
                        int index = 1;
                        while (pieceSymbol == ' ' && index < piece[0].length) {
                            pieceSymbol = piece[0][index++];
                        }
        
                        System.out.println("Placing piece " + pieceSymbol + " at (" + row + ", " + col + ")");
                        placePiece(variant, row, col, pieceSymbol);
                        iterationCount++;
                        printBoard();
                        System.out.println("Iteration: " + iterationCount);
        
                        // Jika solusi ditemukan, hentikan pencarian
                        if (solve(pieceIndex + 1)) return true;
        
                        // Jika tidak berhasil, hapus kembali
                        System.out.println("Removing piece " + pieceSymbol + " from (" + row + ", " + col + ")");
                        removePiece(variant, row, col);
                    }
                }
            }
        }
        





        return false;
    }

    
}