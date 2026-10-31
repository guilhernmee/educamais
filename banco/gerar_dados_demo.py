"""Gera 02_dados_demo.sql: escola de demonstração do Educa+ (mesmos nomes do protótipo).
Rode: python3 gerar_dados_demo.py  (o resultado já vai pronto no pacote)."""
import random, math

R = random.Random(2026)
out = []
w = out.append
q = lambda s: "'" + s.replace("'", "''") + "'"

w("-- Educa+ - dados de demonstração (gerado por gerar_dados_demo.py)")
w("-- Rode depois do 01_schema.sql. As datas são relativas a hoje (NOW()).")
w("USE educa_mais;")
w("SET NAMES utf8mb4;")

SUBJ = {  # codigo: (id_disciplina, regiões)
    'mat': (1, ['Praia da Contagem', 'Floresta das Operações', 'Vulcão dos Desafios']),
    'por': (2, ['Vila das Letras', 'Bosque das Palavras', 'Torre das Histórias']),
    'ing': (3, ['Hello Beach', 'Color Park', 'Number Hill']),
    'cie': (4, ['Jardim dos Seres Vivos', 'Lagoa das Experiências', 'Observatório']),
    'his': (5, ['Aldeia do Passado', 'Estrada do Tempo', 'Museu Secreto']),
    'geo': (6, ['Vale dos Ambientes', 'Montanha dos Mapas', 'Porto do Mundo']),
}
LEVELS = {
    'mat': [('Contar até 20', 'contar', 'contagem'), ('Somar com objetos', 'soma', 'adição'), ('Maior ou menor', 'comparar', 'comparação'),
            ('Formas geométricas', 'formas', 'formas'), ('Sequências', 'sequencia', 'sequências'), ('Problemas de soma', 'problema', 'problemas'),
            ('Somas com dezenas', 'soma2', 'adição com dezenas'), ('Subtração com maçãs', 'subtracao', 'subtração'),
            ('Multiplicação', 'multi', 'multiplicação'), ('Frações', 'fracao', 'frações')],
    'por': [('Vogais', None, 'vogais'), ('Sílabas', None, 'sílabas'), ('Formar palavras', None, 'formar palavras'), ('Completar frases', None, 'completar frases'),
            ('Rimas', None, 'rimas'), ('Plural', None, 'plural'), ('Leitura curta', None, 'leitura de palavras'), ('Interpretação', None, 'interpretação de texto'),
            ('Palavras novas', None, 'ortografia'), ('Desafio das letras', None, 'interpretação de texto')],
    'cie': [('Seres vivos', None, 'seres vivos'), ('Onde vivem os animais', None, 'habitats'), ('Plantas', None, 'plantas'), ('Corpo humano', None, 'corpo humano'),
            ('Água e gelo', None, 'estados da água'), ('Experiências', None, 'experiências'), ('Insetos', None, 'insetos'), ('Reciclagem', None, 'reciclagem'),
            ('Classificar', None, 'seres vivos'), ('Desafio do cientista', None, 'experiências')],
    'his': [('Minha história', None, 'sequência de fatos'), ('Linha do tempo', None, 'linha do tempo'), ('Antigamente', None, 'antigamente'),
            ('Povos indígenas', None, 'povos indígenas'), ('Datas importantes', None, 'datas importantes'), ('Fontes históricas', None, 'fontes históricas'),
            ('Brasil colônia', None, 'linha do tempo'), ('Independência', None, 'datas importantes'), ('Família e tempo', None, 'sequência de fatos'),
            ('Desafio do museu', None, 'linha do tempo')],
    'geo': [('Ambientes', None, 'ambientes'), ('Animais e lugares', None, 'ambientes'), ('Pontos cardeais', None, 'pontos cardeais'), ('Mapa do bairro', None, 'mapas'),
            ('Rios e mares', None, 'ambientes'), ('Paisagens', None, 'ambientes'), ('Brasil no mapa', None, 'mapas'), ('Capitais', None, 'capitais'),
            ('Clima', None, 'ambientes'), ('Desafio do explorador', None, 'mapas')],
    'ing': [('Hello!', None, 'greetings'), ('Colors', None, 'colors'), ('Animals', None, 'animals'), ('Numbers', None, 'numbers'), ('Fruits', None, 'fruits'),
            ('Family', None, 'vocabulário'), ('School', None, 'vocabulário'), ('Toys', None, 'vocabulário'), ('Body', None, 'vocabulário'), ('Final challenge', None, 'vocabulário')],
}
DIFF0 = [1, 1, 1, 2, 2, 2, 2, 2, 3, 3]
REG = [(0, 4), (4, 8), (8, 10)]
ANOS_DISC = {c: ([3, 4, 5] if c == 'ing' else [1, 2, 3, 4, 5]) for c in SUBJ}

# ---------------- trilhas (conteúdo = região, atividade = nível) ----------------
w("\n-- Trilhas do 1º ao 5º ano: 3 regiões e 10 níveis por ilha")
cid = 0; aid = 0
TRILHA = {}  # (ano, codigo) -> [(id_atividade, habilidade)]
for ano in range(1, 6):
    for cod, (did, regs) in SUBJ.items():
        if ano not in ANOS_DISC[cod]:
            continue
        lst = []
        for r, (a, b) in enumerate(REG):
            cid += 1
            w(f"INSERT INTO conteudo (id_conteudo,id_disciplina,titulo,descricao,material,ano_escolar,ordem) VALUES ({cid},{did},{q(regs[r])},{q('Região ' + str(r + 1) + ' da ilha')},{q('Minijogos da região ' + regs[r])},{ano},{r + 1});")
            for k in range(a, b):
                aid += 1
                t, kind, hab = LEVELS[cod][k]
                w(f"INSERT INTO atividade (id_atividade,id_conteudo,titulo,ordem,dificuldade,tipo_minijogo,id_habilidade) VALUES ({aid},{cid},{q(t)},{k - a + 1},{DIFF0[k]},{q(kind) if kind else 'NULL'},(SELECT id_habilidade FROM habilidade WHERE id_disciplina={did} AND nome={q(hab)}));")
                lst.append((aid, hab))
        TRILHA[(ano, cod)] = lst

# ---------------- pessoas ----------------
PROFS = [(1, 'Paulo Andrade', 'paulo.andrade@escolahorizonte.edu.br', 'COORDENADOR', 0),
         (2, 'Carla Nogueira', 'carla.nogueira@escolahorizonte.edu.br', 'PROFESSOR', 0),
         (3, 'Rafael Teixeira', 'rafael.teixeira@escolahorizonte.edu.br', 'PROFESSOR', 0),
         (4, 'Beatriz Lopes', 'beatriz.lopes@escolahorizonte.edu.br', 'PROFESSOR', 2),
         (5, 'Marcos Freitas', 'marcos.freitas@escolahorizonte.edu.br', 'PROFESSOR', 0),
         (6, 'Juliana Prado', 'juliana.prado@escolahorizonte.edu.br', 'PROFESSOR', 4)]
w("\n-- Coordenação e professores")
for i, n, e, p, d in PROFS:
    w(f"INSERT INTO usuario (id_usuario,nome,email,perfil,ultimo_acesso) VALUES ({i},{q(n)},{q(e)},'{p}',DATE_SUB(NOW(), INTERVAL {d * 24 + R.randint(1, 5)} HOUR));")

TURMAS = [(1, '1º A', 1, 3, 22), (2, '2º A', 2, 3, 24), (3, '3º A', 3, 2, 23), (4, '3º B', 3, 4, 21),
          (5, '4º A', 4, 2, 24), (6, '4º B', 4, 2, 22), (7, '5º A', 5, 5, 25), (8, '5º B', 5, 5, 23)]
w("\n-- Turmas e quem ensina cada disciplina (Inglês é sempre a Juliana)")
for tid, nome, ano, prof, n in TURMAS:
    w(f"INSERT INTO turma (id_turma,nome,ano_escolar,ano_letivo) VALUES ({tid},{q(nome)},{ano},YEAR(NOW()));")
    for cod, (did, _) in SUBJ.items():
        if ano in ANOS_DISC[cod]:
            w(f"INSERT INTO disciplina_turma (id_turma,id_disciplina,id_professor,data_vinculo) VALUES ({tid},{did},{6 if cod == 'ing' else prof},MAKEDATE(YEAR(NOW()),33));")

FIRST = ['Ana Clara', 'Miguel', 'Helena', 'Davi', 'Sofia', 'Arthur', 'Laura', 'Gabriel', 'Valentina', 'Heitor', 'Alice', 'Bernardo', 'Manuela', 'Théo', 'Cecília',
         'Lucas', 'Isabela', 'Rafael', 'Lorena', 'Samuel', 'Maria Luiza', 'Enzo', 'Beatriz', 'Pedro', 'Lívia', 'Benjamin', 'Yasmin', 'Matheus', 'Clara', 'Joaquim',
         'Antonella', 'Nicolas', 'Mariana', 'Bento', 'Luna', 'Lorenzo', 'Eloá']
LAST = ['Souza', 'Rocha', 'Duarte', 'Lima', 'Ribeiro', 'Mendes', 'Pires', 'Nunes', 'Costa', 'Barros', 'Cardoso', 'Moreira', 'Araújo', 'Campos', 'Fonseca',
        'Monteiro', 'Batista', 'Ramos', 'Cunha']
DPOOL = {1: [('leitura de palavras', 3), ('contagem', 2), ('subtração', 2)], 2: [('ortografia', 3), ('subtração', 3), ('leitura de palavras', 2)],
         3: [('subtração', 3), ('multiplicação', 3), ('ortografia', 2), ('linha do tempo', 1)], 4: [('frações', 4), ('multiplicação', 3), ('interpretação de texto', 2), ('mapas', 1)],
         5: [('frações', 4), ('multiplicação', 2), ('interpretação de texto', 2), ('mapas', 1)]}
HAB_DISC = {}
for cod, lv in LEVELS.items():
    for _, _, h in lv:
        HAB_DISC[h] = SUBJ[cod][0]
HAB_DISC.update({'leitura de palavras': 2, 'ortografia': 2})
ITEMS = {'CABELO': [('curto', 0), ('cacheado', 0), ('longo', 0), ('coque', 40), ('moicano', 60)],
         'ROUPA': [('laranja', 0), ('azul', 0), ('listrada', 50), ('macacao', 70), ('heroi', 120)],
         'ACESSORIO': [('redondos', 30), ('sol', 45), ('bone', 40), ('mago', 100), ('coroa', 150)],
         'MOCHILA': [('mochila', 35), ('foguete', 110)], 'PET': [('gato', 80), ('cao', 80), ('dragao', 200)]}
SKINS = ['#F6D2B0', '#E8B48A', '#C68B59', '#A0673E', '#6E4426']
HAIRC = ['#3B2416', '#1B1B1B', '#8A4B22', '#E3B04B', '#D9542B']
item = lambda cat, cod: f"(SELECT id_item FROM item_loja WHERE categoria='{cat}' AND codigo='{cod}')"


def wpick(pool):
    tot = sum(p[1] for p in pool); x = R.random() * tot
    for v, wt in pool:
        x -= wt
        if x < 0:
            return v
    return pool[0][0]


LIA = {'mat': [3, 3, 2, 3, 1, 3, 2], 'por': [3, 2, 3, 2], 'cie': [3, 3, 2], 'his': [2, 1], 'geo': [3, 3, 3, 2, 3], 'ing': [2]}
uid = 100; ni = 0
w("\n-- Alunos, partidas, compras e dificuldades")
for tid, tnome, ano, prof, n in TURMAS:
    for k in range(n):
        uid += 1
        if tid == 5 and k == 0:
            nome, email = 'Lia Martins', 'lia.martins@aluno.escolahorizonte.edu.br'
        elif tid == 5 and k == 1:
            nome, email = 'João Pedro Alves', 'joao.alves@aluno.escolahorizonte.edu.br'
        else:
            nome = FIRST[(ni * 7 + 3) % len(FIRST)] + ' ' + LAST[(ni * 11 + 5) % len(LAST)]; ni += 1
            email = nome.lower().replace(' ', '.').translate(str.maketrans('áâãéêíóôõúç', 'aaaeeiooouc')) + f'{uid}@aluno.escolahorizonte.edu.br'
        lia = nome == 'Lia Martins'
        w(f"INSERT INTO usuario (id_usuario,nome,email,perfil) VALUES ({uid},{q(nome)},{q(email)},'ALUNO');")
        w(f"INSERT INTO aluno (id_usuario,id_turma,cor_pele,cor_cabelo) VALUES ({uid},{tid},'{'#C68B59' if lia else R.choice(SKINS)}','{'#3B2416' if lia else R.choice(HAIRC)}');")
        base = R.random() * 40 + 38
        diffs = [] if lia else ([wpick(DPOOL[ano])] if R.random() < .5 else [])
        if not lia and R.random() < .15:
            d2 = wpick(DPOOL[ano])
            if d2 not in diffs:
                diffs.append(d2)
        dias = 0 if lia else (R.randint(6, 12) if R.random() < .12 else R.randint(0, 3))
        # partidas
        plays = []
        for cod in SUBJ:
            if ano not in ANOS_DISC[cod]:
                continue
            tr = TRILHA[(ano, cod)]
            if lia:
                stars = LIA[cod]
            else:
                prog = max(8, min(97, base + (R.random() - .5) * 30 + (-8 if cod == 'mat' and ano >= 4 else 0)))
                done = max(1, round(prog / 10))
                stars = []
                for j in range(done):
                    hab = tr[j][1]
                    pool = [(1, 5), (2, 4), (3, 1)] if hab in diffs else ([(3, 6), (2, 3), (1, 1)] if base > 62 else [(3, 3), (2, 4), (1, 2)])
                    stars.append(wpick(pool))
            for j, st in enumerate(stars):
                plays.append((cod, tr[j][0], st, j))
        R.shuffle(plays)
        plays.sort(key=lambda p: p[3])  # níveis anteriores primeiro
        start = 150 * 24 * 60
        end = dias * 24 * 60 + R.randint(30, 600)
        earned = 0
        for idx, (cod, atv, st, j) in enumerate(plays):
            frac = math.sqrt((idx + 1) / len(plays)) if plays else 1
            ago = int(start - (start - end) * frac)
            if lia:
                ago = int((len(plays) - idx) * 60 * 24 * 1.6 + 90)
            dur = R.randint(2, 7)
            erros = 0 if st == 3 else (R.randint(1, 2) if st == 2 else R.randint(3, 5))
            acertos = 5 if st == 3 else (5 - erros if st == 2 else max(1, 5 - erros))
            earned += st * 10
            w(f"INSERT INTO realizacao_atividade (id_aluno,id_atividade,modo,data_inicio,data_envio,situacao,percentual_acertos,acertos,estrelas,erros) VALUES "
              f"({uid},{atv},'TRILHA',DATE_SUB(NOW(), INTERVAL {ago + dur} MINUTE),DATE_SUB(NOW(), INTERVAL {ago} MINUTE),'ENVIADA',{acertos * 20},{acertos},{st},{erros});")
        if lia:
            w(f"INSERT INTO movimento_estrelas (id_aluno,tipo,quantidade,descricao) VALUES ({uid},'GANHO',100,'Bônus de boas-vindas');")
            earned += 100
        # itens: gratuitos + compras
        owned = [('CABELO', c) for c, p in ITEMS['CABELO'] if p == 0] + [('ROUPA', c) for c, p in ITEMS['ROUPA'] if p == 0]
        bought = []
        if lia:
            bought = [('ACESSORIO', 'redondos', 30)]
        else:
            for _ in range(R.randint(0, 3)):
                cat = R.choice(list(ITEMS)); cod, price = R.choice(ITEMS[cat])
                if price and price <= earned and (cat, cod) not in [(b[0], b[1]) for b in bought]:
                    bought.append((cat, cod, price)); earned -= price
        eq = {'CABELO': 'cacheado' if lia else R.choice(['curto', 'cacheado', 'longo']), 'ROUPA': 'laranja' if lia else R.choice(['laranja', 'azul'])}
        for cat, cod, _ in bought:
            if lia or R.random() < .7:
                eq[cat] = cod
        for cat, cod in owned:
            w(f"INSERT INTO aluno_item (id_aluno,id_item,equipado) VALUES ({uid},{item(cat, cod)},{'TRUE' if eq.get(cat) == cod else 'FALSE'});")
        for cat, cod, price in bought:
            w(f"INSERT INTO aluno_item (id_aluno,id_item,equipado) VALUES ({uid},{item(cat, cod)},{'TRUE' if eq.get(cat) == cod else 'FALSE'});")
            w(f"INSERT INTO movimento_estrelas (id_aluno,tipo,quantidade,id_item,descricao) VALUES ({uid},'COMPRA',{price},{item(cat, cod)},'Compra na loja');")
        for d in diffs:
            w(f"INSERT INTO dificuldade_detectada (id_aluno,id_habilidade,qtd_erros,data_deteccao) VALUES ({uid},(SELECT id_habilidade FROM habilidade WHERE id_disciplina={HAB_DISC[d]} AND nome={q(d)}),{R.randint(2, 5)},DATE_SUB(NOW(), INTERVAL {R.randint(1, 20)} DAY));")
        if nome == 'João Pedro Alves':
            w(f"INSERT INTO aluno_disciplina (id_aluno,id_disciplina,habilitada,id_professor_alteracao) VALUES ({uid},5,FALSE,2);")

# ---------------- questões criadas por professores ----------------
w("\n-- Questões criadas por professores (duas aguardando revisão da coordenação)")
def atv(ano, cod, nivel):
    return TRILHA[(ano, cod)][nivel - 1][0]
QS = [(atv(3, 'cie', 3), 'Qual parte da planta fica embaixo da terra?', ['Raiz', 'Flor', 'Folha'], 'Pense na parte que puxa a água do solo.', 4, 'EM_REVISAO', 'plantas', 4),
      (atv(2, 'por', 7), 'Leia: “O sapo pulou na lagoa.” Onde o sapo pulou?', ['Na lagoa', 'Na árvore', 'Na casa'], 'A resposta está no fim da frase.', 3, 'EM_REVISAO', 'leitura de palavras', 2),
      (atv(5, 'mat', 10), 'A pizza tem 4 fatias e Ana comeu 1. Que fração ela comeu?', ['1/4', '4/1', '3/4'], 'Embaixo vai o total de fatias da pizza.', 5, 'PUBLICADA', 'frações', 1)]
for qi, (a, enun, alts, dica, autor, sit, hab, did) in enumerate(QS, start=1):
    w(f"INSERT INTO questao (id_questao,id_atividade,enunciado,ordem,tipo,dica,id_habilidade,id_autor,situacao) VALUES ({qi},{a},{q(enun)},1,'MULTIPLA_ESCOLHA',{q(dica)},(SELECT id_habilidade FROM habilidade WHERE id_disciplina={did} AND nome={q(hab)}),{autor},'{sit}');")
    for j, t in enumerate(alts):
        w(f"INSERT INTO alternativa (id_questao,texto,correta) VALUES ({qi},{q(t)},{'TRUE' if j == 0 else 'FALSE'});")

open('02_dados_demo.sql', 'w', encoding='utf8').write("\n".join(out) + "\n")
print('ok', len(out), 'linhas; alunos:', uid - 100, '; atividades:', aid)
