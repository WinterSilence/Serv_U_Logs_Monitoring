package Tests;

public class Test {

    private static final char[][] charTable = new char[65536][];

    static {
        charTable['А'] = "A".toCharArray();
        charTable['Б'] = "B".toCharArray();
        charTable['В'] = "V".toCharArray();
        charTable['Г'] = "G".toCharArray();
        charTable['Д'] = "D".toCharArray();
        charTable['Е'] = "E".toCharArray();
        charTable['Ё'] = "E".toCharArray();
        charTable['Ж'] = "ZH".toCharArray();
        charTable['З'] = "Z".toCharArray();
        charTable['И'] = "I".toCharArray();
        charTable['Й'] = "I".toCharArray();
        charTable['К'] = "K".toCharArray();
        charTable['Л'] = "L".toCharArray();
        charTable['М'] = "M".toCharArray();
        charTable['Н'] = "N".toCharArray();
        charTable['О'] = "O".toCharArray();
        charTable['П'] = "P".toCharArray();
        charTable['Р'] = "R".toCharArray();
        charTable['С'] = "S".toCharArray();
        charTable['Т'] = "T".toCharArray();
        charTable['У'] = "U".toCharArray();
        charTable['Ф'] = "F".toCharArray();
        charTable['Х'] = "H".toCharArray();
        charTable['Ц'] = "C".toCharArray();
        charTable['Ч'] = "CH".toCharArray();
        charTable['Ш'] = "SH".toCharArray();
        charTable['Щ'] = "SH".toCharArray();
        charTable['Ъ'] = "'".toCharArray();
        charTable['Ы'] = "Y".toCharArray();
        charTable['Ь'] = "'".toCharArray();
        charTable['Э'] = "E".toCharArray();
        charTable['Ю'] = "U".toCharArray();
        charTable['Я'] = "YA".toCharArray();

        for (int i = 0; i < charTable.length; i++) {
            char idx = (char) i;
            char lower = new String(new char[]{idx}).toLowerCase().charAt(0);
            if (charTable[i] != null) {
                charTable[lower] = toLowerCase(charTable[i]);
            }
        }
    }

    private static char[] toLowerCase(char[] chars) {
        char[] r = new char[chars.length];
        for (int i = 0; i < chars.length; i++) {
            r[i] = Character.toLowerCase(chars[i]);
        }
        return r;
    }

    public static void main(String[] args) {
        Test test = new Test();
        System.out.println(test.renameCirillic("Привет, Мир. Это Длинная Строка с Разными символами русского алфавита."));
    }

    private String renameCirillic(String filename) {
        StringBuilder sb = new StringBuilder(filename.length());
        for (int i = 0; i < filename.length(); i++)
        {
            char[] replace = charTable[filename.charAt(i)];
            if (replace == null)
            {
                sb.append(filename.charAt(i));
            }
            else
            {
                sb.append(replace);
            }
        }
        return sb.toString();
    }
}
