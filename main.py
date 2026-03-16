from kivy.app import App
from kivy.uix.label import Label
from kivy.uix.boxlayout import BoxLayout

class M22Interface(BoxLayout):
    def __init__(self, **kwargs):
            super().__init__(**kwargs)
                    self.add_widget(Label(
                                    text='M22 Reader\n[Modo Nativo Ativado]', 
                                                font_size='30sp',
                                                            halign='center'
                                                                    ))

                                                                    class M22ReaderApp(App):
                                                                        def build(self):
                                                                                return M22Interface()

                                                                                if __name__ == '__main__':
                                                                                    M22ReaderApp().run()
                                                                                    
                    ))
