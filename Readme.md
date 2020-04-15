Параллельная реализация находится в классе RunnerMultithreadingImpl, реализация для одного потока в классе RunnerSimpleImpl.

Чтобы оценить эффективность распраллеливания можно запустить EfficiencyTest.kt  
Методика оценки довольно грубая и понятно что все очень сильно зависит от того сколько времени занимает именно выполнение процессоров  
В целом получилось примерно так:
Efficiency for 1 threads: 0.9970586  
Efficiency for 2 threads: 0.99588716  
Efficiency for 3 threads: 0.9628662  
Efficiency for 4 threads: 0.96939784  
Efficiency for 5 threads: 0.9717658  
Efficiency for 6 threads: 0.97804916  
Efficiency for 7 threads: 0.94240177  
Efficiency for 8 threads: 0.8926984  
Efficiency for 9 threads: 0.90747106   
Efficiency for 10 threads: 0.9323598  
Efficiency for 11 threads: 0.89472294  
Efficiency for 12 threads: 0.8964321  
Efficiency for 13 threads: 0.8932165  
Efficiency for 14 threads: 0.85136473  
Efficiency for 15 threads: 0.8433379  
Efficiency for 16 threads: 0.76336676

Тесты в RunnerMultithreadingImplTest проверяют что ответы для параллельного и не параллельной реализаций совпадают.  
Во избежание проблем с гонкой между null и exception, обсужденной нами в письме, в случайных тестах процессоры никогда не бросают исключения.