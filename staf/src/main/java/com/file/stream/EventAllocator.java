package com.file.stream;//@date 30.03.2022

import com.file.FragmentFileScanner;

public interface EventAllocator {

    LangEvent allocate(FragmentFileScanner fragmentFileScanner);
}
