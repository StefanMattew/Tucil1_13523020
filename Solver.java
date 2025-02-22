import java.io.*;
import java.util.*;

public class Solver {
    private static int rows, cols, count_piece;
    private static String type_board;
    private static char[][] board;
    private static List<char[][]> pieces = new ArrayList<>();
    private static long iterationCount;

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

    private static final String reset_color = "\u001B[0m";


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Masukkan path file(.txt): ");
        String fileName = scanner.nextLine();
        

        if (!cekReadFile(fileName)) {
            System.out.println("File tidak valid!");
            scanner.close();
            return;
        }

        long start = System.currentTimeMillis();

        boolean solved = solve(0);

        long end = System.currentTimeMillis();

        if (solved) {
            printBoard();
        } else {
            System.out.println("Tidak ditemukan solusi.\n");
        }
        System.out.println("Waktu pencarian: " + (end - start) + " ms\n");
        System.out.println("Banyak kasus yang ditinjau: " + iterationCount +"\n");

        System.out.print("Apakah anda ingin menyimpan solusi? (ya/tidak): ");
        if (scanner.nextLine().equalsIgnoreCase("ya")) {
            saveFile("Solusi "+fileName );
        }
        scanner.close();
    }
    

    public static boolean cekReadFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String[] info = reader.readLine().split(" ");
            
            if (info.length < 3) return false;


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
                line = line.stripTrailing();    // delete spasi after piece
                if( line.isEmpty()){
                    continue;                   //skip baris kosong
                }

                char firstChar = line.trim().charAt(0);    //mengambil char pertama (bukan spasi)

                if ( temp.isEmpty() || firstChar== temp.charAt(0) ){  // jika karakter masi sama atau pertama
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
        char[][] copy = new char[r][c];
    
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


    public static List<char[][]> createVariation(char[][] piece){
        Set<String> uniquePiece = new HashSet<>();
        List<char[][]> variations = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            piece = rotate(piece);        //rotasi dicek 4 kali

            if (uniquePiece.add(Arrays.deepToString(piece))) {
                variations.add(copyMatriks(piece));  // tambahkan variasi dari rotate
            }

            char[][] mirroredPiece = mirror(piece); // mirror hasil rotasi
            if (uniquePiece.add(Arrays.deepToString(mirroredPiece))) {
                variations.add(copyMatriks(mirroredPiece)); // tambah hasil pencerminan
            }


        }
        return variations;
    }
    public static boolean solve (int pieceIndex){
        if (pieceIndex >= pieces.size()){
            return true;
        }
        
        char[][] piece = pieces.get(pieceIndex);

        List<char[][]> variations = createVariation(piece);
        
        
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
        
                            // jika solusi ditemukan
                        if (solve(pieceIndex + 1)) return true;
        
                        // jika tidak berhasil
                        System.out.println("Removing piece " + pieceSymbol + " from (" + row + ", " + col + ")");
                        removePiece(variant, row, col);
                    }
                }
            }
        }
        return false;
    }

    public static char[][] rotate (char[][] piece){ //rotate 90 degree
        int h = piece.length;
        int w = piece[0].length;
        char[][] rotated = new char[w][h];
        for (int i=0;i<h; i++ ){
            for (int j=0 ; j<w ; j++){
                rotated[j][h-1-i]= piece[i][j];
            }
        }
        return rotated;
    }

    public static char[][] mirror (char[][] piece){ //mirror
        int h = piece.length;
        int w = piece[0].length;
        char[][] mirrored = new char[h][w];
        for (int i=0;i<h; i++ ){
            for (int j=0 ; j<w ; j++){
                mirrored[i][w-1-j]= piece[i][j];
            }
        }
        return mirrored;


    }
    public static boolean canPlace(char[][] piece, int r, int c){
        int h = piece.length;

        for (int i =0 ; i<h ; i++){
            for (int j=0; j<piece[i].length ; j++){
                if (piece[i][j] != ' ' && board [r + i][c+j] != '.'){
                    return false;
                }
            }
        }
        return true;
    }

    public static void placePiece(char[][]piece, int r, int c, char var ){

        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                if (piece[i][j] != ' ') {
                    board[r + i][c + j] = var;
                }
            }
        }
    }

    public static void removePiece (char[][] piece, int r , int c){


        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                if (piece[i][j] != ' ') {
                    board[r + i][c + j] = '.';
                }
            }
        }
    }

 
    public static void printBoard() {
        Map<Character, String> colorMap = new HashMap<>();
        
        for (char c = 'A'; c <= 'Z'; c++) {
            colorMap.put(c, color[(c - 'A') % color.length]);
        }
        
        for (char[] row : board) {
            for (char isi : row) {
                if (isi == '.') {
                    System.out.print(isi + " ");
                } else {
                    System.out.print(colorMap.get(isi) + isi + reset_color + " ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }
    
    public static void saveFile(String fileOutput){
        try (PrintWriter writer = new PrintWriter(fileOutput)) {
            for (char[] row : board) {
                writer.println(new String(row));
            }
            System.out.println("File telah berhasil disimpan di " + fileOutput);

        } catch (IOException e) {
            System.out.println("Gagal menyimpan solusi.");
        }
    
    }

}