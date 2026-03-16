from flask import Flask, render_template
import os

app = Flask(__name__)

# Onde os teus Manhwas estão guardados no Android
CAMINHO_MANHWAS = os.path.expanduser("~/storage/downloads")

@app.route('/')
def home():
    formatos = ('.pdf', '.epub', '.cbz', '.cbr')
    try:
        # Lista apenas ficheiros que são quadrinhos ou livros
        ficheiros = [f for f in os.listdir(CAMINHO_MANHWAS) if f.lower().endswith(formatos)]
    except Exception as e:
        ficheiros = []
        print(f"Erro ao aceder pasta: {e}")
    
    return render_template('index.html', livros=ficheiros)

if __name__ == '__main__':
    print("\n--- M22 READER ONLINE ---")
    print("Acede a: http://localhost:8080")
    print("--------------------------\n")
    app.run(host='0.0.0.0', port=8080, debug=True)
