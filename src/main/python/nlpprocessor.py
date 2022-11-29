import spacy

nlp = None
processed_text = None

def load_model():
    global nlp 
    nlp = spacy.load("en_core_web_sm")

def process_text(text):
    global processed_text 
    processed_text = nlp(text)

def main():
    nlp = spacy.load("en_core_web_sm")
    print("test")

def get_num_tokens():
    return len(processed_text)

if __name__ == "__main__":
    main()