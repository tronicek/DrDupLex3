package p6;

public class A {

    private void matinv(double[][] A, double[][] I, int nelem) throws SingMatrixException {
        for (int i = 0; i < nelem; i++) {
            for (int j = 0; j < nelem; j++) {
                I[i][j] = 0.0;
            }
            I[i][i] = 1.0;
        }
        for (int diag = 0; diag < nelem; diag++) {
            if (!dopivot(A, I, diag, nelem)) {
                throw new SingMatrixException();
            }
            double div = A[diag][diag];
            if (div != 1.0) {
                A[diag][diag] = 1.0;
                for (int j = diag + 1; j < nelem; j++) A[diag][j] /= div;
                for (int j = 0; j < nelem; j++) I[diag][j] /= div;
            }
            for (int i = 0; i < nelem; i++) {
                if (i == diag) continue;
                double sub = A[i][diag];
                if (sub != 0.0) {
                    A[i][diag] = 0.0;
                    for (int j = diag + 1; j < nelem; j++) A[i][j] -= sub * A[diag][j];
                    for (int j = 0; j < nelem; j++) I[i][j] -= sub * I[diag][j];
                }
            }
        }
    }
}
